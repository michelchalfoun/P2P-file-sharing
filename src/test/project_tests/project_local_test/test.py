# with open('logs/log_peer_1004.log') as f:
#     lines = f.readlines()
#     l2 = [x for x in lines if 'has downloaded the piece' in x]
#     index = [x[60:65].strip() for x in l2]
#     index2 = [int(x[0:x.index(' ')]) for x in index]

#     emptyDict = {}
#     for i in range(674):
#         emptyDict[i] = 0
#     for i in index2:
#         emptyDict[i] = emptyDict[i] + 1

#     for i in range(674):
#         if emptyDict[i] != 1:
#             print(str(i) + " is missing: " + str(emptyDict[i]))

with open('logs/log_peer_1004.log') as f:
    lines = f.readlines()
    l2 = [x for x in lines if '\'have\' message from 1002' in x]
    index2 = [int(x[86:113].strip().replace('.', '')) for x in l2]

    emptyDict = {}
    for i in range(674):
        emptyDict[i] = 0
    for i in index2:
        emptyDict[i] = emptyDict[i] + 1

    # print(emptyDict)

    counter = 0

    for i in range(674):
        # print(str(i) + ": "+ str(emptyDict[i]))
        if emptyDict[i] >= 1:
            counter += 1
            # print(str(i) + " is missing: " + str(emptyDict[i]))
            print(str(i) + ": "+ str(emptyDict[i]))

    print(counter)

    # print(l2)
    # for i in l2:
        # print(i)
