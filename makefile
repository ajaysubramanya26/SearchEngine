run:
	mvn clean package
	-ZIP -d target/ska_ir_system.jar META-INF/LICENSE
	time java -jar target/ska_ir_system.jar

