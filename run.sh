mvn clean package
rm src/test/project_tests/project_local_test/peerProcess.jar
rm src/test/project_tests/project_config_file_large/peerProcess.jar
rm src/test/project_tests/project_config_file_small/peerProcess.jar
rm transfer/peerProcess.jar

cp target/peerProcess.jar src/test/project_tests/project_local_test
cp target/peerProcess.jar src/test/project_tests/project_config_file_large
cp target/peerProcess.jar src/test/project_tests/project_config_file_small
cp target/peerProcess.jar transfer/peerProcess.jar