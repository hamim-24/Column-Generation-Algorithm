package util;

public class utils 
{
    public static final int UN_REALISTIC_VALUE = 1000000;

    public static void header(String str) 
    {
        System.out.println("\n");
        System.out.println("=".repeat(str.length() + 30));
        System.out.println(" ".repeat(15) + str + " ".repeat(15));
        System.out.println("=".repeat(str.length() + 30));
    }
    public static void subHeader(String str)
    {
        int num = 5;
        System.out.println();
        System.out.print("=".repeat(num));
        System.out.print("  " + str + "  ");
        System.out.println("=".repeat(num));
    }
}
