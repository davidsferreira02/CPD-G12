all: buildJava buildCpp
	mkdir -p ./tests/results/

buildJava:
	javac matrixproduct.java

buildCpp:
	g++ matrixproduct.cpp -o matrixproduct -O2 -lpapi

clean: 
	rm -f MatrixProduct.class matrixproduct
	rm -r -f ./tests/results/*.csv
	

test1:
	rm -r -f ./tests/results/test1*.csv
	touch ./tests/results/test1-c.csv ./tests/results/test1-java.csv
	./matrixproduct -t < ./tests/input/test1.txt > ./tests/results/test1-c.csv
	java MatrixProduct -t < ./tests/input/test1.txt > ./tests/results/test1-java.csv
	
test2:
	rm -r -f ./tests/results/test2*.csv
	touch ./tests/results/test2-c.csv ./tests/results/test2-2-c.csv ./tests/results/test2-java.csv
	./matrixproduct -t < ./tests/input/test2.txt > ./tests/results/test2-c.csv
	java MatrixProduct -t < ./tests/input/test2.txt > ./tests/results/test2-java.csv
	./matrixproduct -t < ./tests/input/test2-2.txt > ./tests/results/test2-2-c.csv

test3:
	rm -r -f ./tests/results/test3*.csv
	touch ./tests/results/test3-c.csv
	./matrixproduct -t < ./tests/input/test3.txt > ./tests/results/test3-c.csv


tests: clean all test1 test2 test3
	
