package pieces;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Pieces {
    private final AtomicReferenceArray<Boolean> bitfield;
    private final AtomicInteger numberOfPiecesDownloaded;
    private final ReentrantReadWriteLock lock;
    private final int numberOfPieces;

    public Pieces(final AtomicReferenceArray<Boolean> bitfield, final int numberOfDownloadedPieces) {
        this.bitfield = bitfield;
        numberOfPiecesDownloaded = new AtomicInteger(numberOfDownloadedPieces);
        lock = new ReentrantReadWriteLock();
        numberOfPieces = bitfield.length();
    }

    public Pieces(final int numberOfPieces) {
        this.numberOfPieces = numberOfPieces;
        bitfield = new AtomicReferenceArray<>(numberOfPieces);
        numberOfPiecesDownloaded = new AtomicInteger(0);
        lock = new ReentrantReadWriteLock();

        lock.writeLock().lock();
        for (int i = 0; i < numberOfPieces; i++) {
            bitfield.set(i, false);
        }
        lock.writeLock().unlock();
    }

    public int hasAndSet(final int pieceID) {
        lock.writeLock().lock();
        int updatedNumberOfPiecesDownloaded = -1;
        if (bitfield.compareAndSet(pieceID, false, true)) {
            updatedNumberOfPiecesDownloaded = numberOfPiecesDownloaded.incrementAndGet();
        }
        lock.writeLock().unlock();
        return updatedNumberOfPiecesDownloaded;
    }

    public boolean hasPiece(final int peerID) {
        lock.readLock().lock();
        final boolean hasPiece = bitfield.get(peerID);
        lock.readLock().unlock();
        return hasPiece;
    }

    public int getNumberOfPiecesDownloaded() {
        lock.readLock().lock();
        final int piecesDownloaded = numberOfPiecesDownloaded.get();
        lock.readLock().unlock();
        return piecesDownloaded;
    }

    public ReentrantReadWriteLock.ReadLock getReadLock() {
        return lock.readLock();
    }

    public int getNumberOfPieces() {
        return numberOfPieces;
    }

    public AtomicReferenceArray<Boolean> getBitfield() {
        return bitfield;
    }

    public int getRandomPiece(final Pieces neighborPieces) {
        lock.readLock().lock();
        neighborPieces.getReadLock().lock();

        int randomPieceID = -1;
        final ArrayList<Integer> missingPieces = new ArrayList<>();
        for (int index = 0; index < numberOfPieces; index++) {
            if (neighborPieces.getBitfield().get(index) && !bitfield.get(index)) {
                missingPieces.add(index);
            }
        }

        if (missingPieces.size() != 0) {
            int randomNum = ThreadLocalRandom.current().nextInt(0, missingPieces.size());
            randomPieceID = missingPieces.get(randomNum);
        }

        lock.readLock().unlock();
        neighborPieces.getReadLock().unlock();

        return randomPieceID;
    }

    public boolean isInterested(final Pieces neighborPieces) {
        lock.readLock().lock();
        neighborPieces.getReadLock().lock();
        boolean isInterested = false;

        for (int index = 0; index < neighborPieces.getNumberOfPieces(); index++) {
            if (neighborPieces.getBitfield().get(index) && !bitfield.get(index)) {
                isInterested = true;
            }
        }
        lock.readLock().unlock();
        neighborPieces.getReadLock().unlock();
        return isInterested;
    }
}
