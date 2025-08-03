## JMatrix

JMatrix is a lightweight Java package developed as a learning project to explore matrix operations. It provides basic functionality for 2D matrix calculations such as addition, multiplication, and aggregation, with some operations optimized using multithreading. The package is designed to be simple and easy to use, making it a practical tool for those interested in understanding the fundamentals of numerical computing in Java.

<img width="1536" height="1024" src="https://github.com/user-attachments/assets/9592ddaf-f745-4d34-9ffc-93996f58a6fb" />

## Current Benchmarks

The computation metrics are recorded for a 2 dimensional floating point matrix of size 1000X1000. Therefore, a total of 2*10e6 FLOPS including the scalar addition and multiplication that are involved.

|			Operation			   |	Execution Time	|	
|----------------------------------|--------------------------------|
| Matrix multiplication (Naive implementation)   |			> 10 minutes 	    |
| Matrix multiplication (Parallel) |  			0.034605248 	seconds    | 

## Installation

To use **JMatrix**, follow these steps:

1. Clone the repository  
   ```bash
   git clone https://github.com/nithinbharathi/jmatrix.git
   cd jmatrix
2. Build and install the package locally using Maven
	```bash
	mvn clean install
3. Add the jar file (found under jmatrix/target/) to your project's build path


## Usage

Below is a contrived example of some of the operations the package supports.

```Java
package import
import jmatrix.Matrix;
	
public class JTest{
	public static void main(String[] args) {
		double a[][] = {{1,2,3},{3,4,7}};
		double b[][] = {{1,2,3},{3,4,7},{1,2,3}};

		Matrix m1 = new Matrix(a);
		Matrix m2 = new Matrix(b);

		Matrix res = m1.multiply(m2);

		res.view();
	}
}
```
