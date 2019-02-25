import com.meeting.ImproveInMinistry;
import com.meeting.LivingAsChristians;
import com.meeting.Meeting;
import com.meeting.Treasures;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

class App {

    public static void main(String[] args) {
        ContentParser contentParser = new ContentParser();
        File XHTML_FILE_1 = new File(".content/mwb_AM_201905/202019170.xhtml");
        ArrayList<Meeting> meetings = null;
        try {
            meetings = contentParser.parse(XHTML_FILE_1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Meeting meeting : meetings) {
            switch (meeting.getKind()) {
                case Meeting.TREASURES:
                    System.out.println(((Treasures) meeting).getTitle());
                    break;
                case Meeting.IMPROVE_IN_MINISTRY:
                    ImproveInMinistry improveInMinistry = (ImproveInMinistry) meeting;
                    for (String demonstration : improveInMinistry.getDemonstrations()) {
                        System.out.println(demonstration);
                    }
                    break;
                case Meeting.LIVING_AS_CHRISTIANS:
                    LivingAsChristians livingAsChristians = (LivingAsChristians) meeting;
                    for (String part : livingAsChristians.getParts()) {
                        System.out.println(part);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
