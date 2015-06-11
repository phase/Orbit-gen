package solar.dimensions.orbit.test.example;

import solar.dimensions.orbit.API;

@API
public class Example {
    
    public static void main(String... args){
        System.out.println("In Example class!");
    }
    
    @API
    public int two(){
        return 2;
    }
    
    @API
    public boolean returnFalse(){
        return false;
    }
    
    /* What is should output:
    public interface Example{
        public int two();
        public boolean false();
    }
    */
    
}