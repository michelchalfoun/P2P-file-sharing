mvn clean package
rm project_tests/project_local_test/peerProcess.jar
rm project_tests/project_config_file_large/peerProcess.jar
rm project_tests/project_config_file_small/peerProcess.jar
rm peerProcess.jar

cp target/peerProcess.jar project_tests/project_local_test
cp target/peerProcess.jar project_tests/project_config_file_large
cp target/peerProcess.jar project_tests/project_config_file_small
cp target/peerProcess.jar peerProcess.jar