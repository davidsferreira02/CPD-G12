all: buildJava buildCpp

buildJava:
	javac matrixproduct.java

buildCpp:
	g++ matrixproduct.cpp -o matrixproduct -O2 -lpapi

clean: 
	rm -f MatrixProduct.class matrixproduct
	rm -r -f ./tests/results/*.txt

test1:
	rm -r -f ./tests/results/test1*.txt
	touch ./tests/results/test1-c.txt ./tests/results/test1-java.txt
	./matrixproduct < ./tests/input/test1.txt > ./tests/results/test1-c.txt
	java MatrixProduct < ./tests/input/test1.txt > ./tests/results/test1-java.txt
	
test2:
	rm -r -f ./tests/results/test2*.txt
	touch ./tests/results/test2-c.txt touch ./tests/results/test2-2-c.txt ./tests/results/test2-java.txt
	./matrixproduct < ./tests/input/test2.txt > ./tests/results/test2-c.txt
	java MatrixProduct < ./tests/input/test2.txt > ./tests/results/test2-java.txt
	./matrixproduct < ./tests/input/test2-2.txt > ./tests/results/test2-2-c.txt

test3:
	rm -r -f ./tests/results/test3*.txt
	touch ./tests/results/test3-c.txt
	./matrixproduct < ./tests/input/test3.txt > ./tests/results/test3-c.txt


tests: clean all test1 test2 test3
	
