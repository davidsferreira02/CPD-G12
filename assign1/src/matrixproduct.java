import java.util.*;

class MatrixProduct {

    public static void OnMult(int m_ar, int m_br){

        double temp=0;
        int i=0;
        int j=0;
        int k=0;
        long time1=0;
        long time2=0;
    
        char c=' ';
    
        double [] pha;
        double [] phb;
        double [] phc;
    
        pha=new double[m_ar*m_br];
        phb=new double[m_ar*m_br];
        phc=new double[m_ar*m_br];
    
        for(i=0; i<m_ar; i++)
            for(j=0; j<m_ar; j++)
                pha[i*m_ar + j] = (double)1.0;
    
    
    
        for(i=0; i<m_br; i++)
            for(j=0; j<m_br; j++)
                phb[i*m_br + j] = (double)(i+1);
    
    
        time1 = System.nanoTime();
    
          for(i=0; i<m_ar; i++)
          {	for( j=0; j<m_br; j++)
            {	temp = 0;
              for( k=0; k<m_ar; k++)
              {	
                temp += pha[i*m_ar+k] * phb[k*m_br+j];
              }
              phc[i*m_ar+j]=temp;
            }
          }
        
          time2 = System.nanoTime();
          long diferença=time2-time1;
          System.out.printf("Time: %.3f seconds\n",diferença / 1_000_000_000d);
        
          // display 10 elements of the result matrix tto verify correctness
          System.out.println( "Result matrix: ");
          for (i = 0; i < 1; i++) {
            for (j = 0; j < Math.min(10, m_br); j++) {
                System.out.printf( "%.0f ",phc[j]);
            }
            }
          
        
            pha=null;
            phb=null;
            phc=null;
          
    
    }

    public static void OnMultLine(int m_ar, int m_br){
        
        long Time1, Time2;
        int i, j, k;
        double[] pha = new double[m_ar * m_ar];
        double[] phb = new double[m_ar * m_ar];
        double[] phc = new double[m_ar * m_ar];

        for(i=0; i<m_ar; i++)
            for(j=0; j<m_ar; j++)
                pha[i*m_ar + j] = (double)1.0;



        for(i=0; i<m_br; i++)
            for(j=0; j<m_br; j++)
                phb[i*m_br + j] = (double)(i+1);

        Time1 = System.currentTimeMillis();
        //code here
        for(i=0; i<m_ar; i++) {
            for(k=0; k<m_br; k++) {
                for(j=0; j<m_ar; j++) {
                    phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
                }
            }
        }

        Time2 = System.currentTimeMillis();

        System.out.printf("Time: %3.3f seconds\n", (double)(Time2 - Time1)/1000);


        // display 10 elements of the result matrix tto verify correctness
        System.out.println("Result matrix: ");
        for(i=0; i<1; i++)
        {	for(j=0; j<Math.min(10,m_br); j++)
            System.out.print(phc[j] + " ");
        }
        System.out.println();



    }
    public static void main(String[] args) {
       
        char c;
        int lin, col, blockSize;
        int op;
        Scanner input = new Scanner(System.in);
        
        

        op=1;
	do {
		System.out.println("1. Multiplication");
		System.out.println("2. Line Multiplication");
		System.out.println("3. Block Multiplication");
		System.out.println("Selection?: ");
		op = input.nextInt();
		if (op == 0)
			break;
            System.out.println("Dimensions: lins=cols ? ");
            lin = input.nextInt();
            col = lin;


            switch (op){
                case 1: 
                    OnMult(lin, col);
                    break;
                case 2:
                    OnMultLine(lin, col);  
                    break;
                case 3:
                    System.out.println("Block Size? ");
                    blockSize = input.nextInt();
                    //OnMultBlock(lin, col, blockSize);  
                    break;

		}




	}while (op != 0);
    }
}