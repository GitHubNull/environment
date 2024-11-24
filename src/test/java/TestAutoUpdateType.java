import oxff.org.model.AutoUpdateType;
import oxff.org.utils.Tools;

public class TestAutoUpdateType {
    public static void main(String[] args)
    {
        AutoUpdateType test = AutoUpdateType.getAutoUpdateType("UUID");
        switch (test){
            case UUID:
                System.out.println(Tools.getRandomUUID());
                break;
            case TIMESTAMP:
                System.out.println(Tools.getTimestamp());
                break;
            case SHA1_OF_TIMESTAMP:
                System.out.println(Tools.sha1(Tools.getTimestamp()));
                break;
            case RANDOM_NUMBER:
                System.out.println(Tools.getRandomNumber(5));
                break;
            case RANDOM_TEXT:
                System.out.println(Tools.getRandomText(5));
                break;
                default:
                    System.out.println("default");
        }
    }
}
