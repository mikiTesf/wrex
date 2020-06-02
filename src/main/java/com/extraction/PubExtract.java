package com.extraction;

import com.meeting.Meeting;

import javax.swing.ImageIcon;
import java.util.ArrayList;

public class PubExtract {

    private String publicationName;
    private ImageIcon publicationImage;
    private ArrayList<Meeting> meetings;

    PubExtract() {}

    PubExtract(String publicationName, ImageIcon publicationImage, ArrayList<Meeting> meetings)
    {
        this.publicationName = publicationName;
        this.publicationImage = publicationImage;
        this.meetings = meetings;
    }

    public String getPublicationName() {
        return publicationName;
    }

    public void setPublicationName(String publicationName) {
        this.publicationName = publicationName;
    }

    public ImageIcon getPublicationImage() {
        return publicationImage;
    }

    public void setPublicationImage(ImageIcon publicationImage) {
        this.publicationImage = publicationImage;
    }

    public ArrayList<Meeting> getMeetings() {
        return meetings;
    }

    public void setMeetings(ArrayList<Meeting> meetings) {
        this.meetings = meetings;
    }
}
