public class test  {

    public static void main(String[] args) {
        int[] arr = {2,5,3,6,4};

        for (int i = 0; i < arr.length; i++) {
            for (int k = 0; k < arr.length-i-1; k++) {
                int empty = 0;
                if (arr[k]>arr[k+1]){
                    empty = arr[k];
                    arr[k] = arr[k+1];
                    arr[k+1] = empty;
                }
            }
        }

        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i]+" ");
        }
    }
}
