rm -rf project_tests/project_local_test/1002 && rm -rf project_tests/project_local_test/1003 && rm -rf project_tests/project_local_test/1004
rm -rf project_tests/project_local_test/logs
mvn clean package
rm project_tests/project_local_test/peerProcess.jar
mv target/peerProcess.jar project_tests/project_local_test