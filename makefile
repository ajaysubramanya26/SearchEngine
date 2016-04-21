run_ask_ir:
	mvn clean package
	-ZIP -d target/ask_ir_system.jar META-INF/LICENSE
	time java -jar target/ask_ir_system.jar

