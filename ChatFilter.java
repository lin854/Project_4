import java.io.*;
public class ChatFilter {
    private String badWordFileName;
    public ChatFilter(String badWordsFileName) {
        this.badWordFileName = badWordsFileName;
    }

    public String filter(String msg) {
        try {
            File file = new File(badWordFileName);
            FileReader fr = new FileReader(file);
            BufferedReader bfr = new BufferedReader(fr);
            int lineCount =0;
            while(bfr.readLine() != null){
                lineCount++;
            }
            bfr.close();

            String[] array = new String[lineCount];
            FileReader fr2 = new FileReader(file);
            BufferedReader bfr2 = new BufferedReader(fr2);
            for(int i =0; i < array.length; i++){
                array[i] = bfr2.readLine();
            }
            bfr2.close();
            for(int k =0; k < array.length; k++){
                int j =0;
                for(int i = array[k].length(); i <= msg.length(); i++) {
                    String badWord = msg.substring(j, i).toLowerCase();
                    if(badWord.equals(array[k])){
                        String wordBefore = msg.substring(0, j);
                        String wordAfter = msg.substring(i, msg.length());
                        String newWord = "*";
                        for(int m =0; m < badWord.length()-1; m++) {
                            newWord += "*";
                        }
                        msg = wordBefore + newWord +  wordAfter;
                    }
                    j++;
                }
            }


        }catch (IOException e){
            e.printStackTrace();
        }
        return msg;
    }

}
