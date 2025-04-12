import java.util.Scanner;
/**
 * Write a description of class Main here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Main
{
    public static void main(String[] args){
        Scanner ui=new Scanner(System.in);
        System.out.println("Welcome to the Crossroad Game, please enter the round you would like to play (1-3), in ascending difficulty");
        int level=ui.nextInt();
        if(level==1){
            Game.main(args);
        }else if(level==2){
            
        }else{
            System.out.println("Invalid Input");
        }
    }
}