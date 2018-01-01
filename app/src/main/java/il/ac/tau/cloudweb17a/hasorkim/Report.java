package il.ac.tau.cloudweb17a.hasorkim;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Report implements  java.io.Serializable{


    private String id;
    private String reporterName;
    private int incrementalReportId;
    private String status;
    private String startTime;
    private String address;
    private String[] imageUrls;
    private String freeText;
    private String phoneNumber;
    private String extraPhoneNumber;
    private String assignedScanner;
    private int availableScanners;
    private String cancellationText;
    private String userId;
    private boolean hasSimilarReports;

    private int nextIncrementalId;
    private static final String TAG = "Report";

    public Report(){
        // Default constructor required for calls to DataSnapshot.getValue(Report.class)
    }


    public Report(String address, String freeText, User user) {

        this.startTime = Calendar.getInstance().getTime().toString();
        this.address = address;
        this.status = "NEW";
        this.freeText = freeText;

        this.assignedScanner = "";
        this.availableScanners = 0;
        this.reporterName = user.getName();
        this.phoneNumber = user.getPhoneNumber();
        this.userId = user.getId();
        //this.incrementalReportId = this.setIncrementalReportId();

        this.hasSimilarReports=false;
        setLisetnerOnReportWithUserId();
    }

    private void setLisetnerOnReportWithUserId() {
        if(userId!=null) {
            // Initialize Database
            Query sameReportQuery = FirebaseDatabase.getInstance().getReference()
                    .child("reports").orderByChild("userId").equalTo(userId);


            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, userId);
                    Log.d(TAG, String.valueOf(dataSnapshot.getChildrenCount()));
                    if (dataSnapshot.getChildrenCount() > 0) {
                        hasSimilarReports = true;
                    } else {
                        hasSimilarReports = false;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    //do nothing
                }
            };


            sameReportQuery.addValueEventListener(postListener);

        }
    }


    public String getId() {return id;   }

    public String getReporterName() {
        return reporterName;
    }

    public String getFreeText() {
        return freeText;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getExtraPhoneNumber() {
        return extraPhoneNumber;
    }

    public String getAssignedScanner() {
        return assignedScanner;
    }

    public String getStatus() { return this.status; }

    public String getStartTime() { return this.startTime; }

    public String getAddress() { return this.address; }
    public String getUserId() {return userId;   }
    public int getAvailableScanners() { return this.availableScanners; }

    public String getCancellationText() {
        return cancellationText;
    }

    public int getIncrementalReportId() { return incrementalReportId; }

    public void persistReport(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports");
        reportsRef.push().setValue(this);
    }

    public void setExtraPhoneNumber(String extraPhoneNumber) {
        this.extraPhoneNumber = extraPhoneNumber;
    }

    public void setReporterName(String reporterName,User user) {
        this.reporterName = reporterName;
        user.setName(reporterName);
    }

    public void setPhoneNumber(String phoneNumber, User user) {
        this.phoneNumber=phoneNumber;
        user.setPhoneNumber(phoneNumber);
    }

    public void setMoreInformation(String moreInformation) {
        this.freeText = moreInformation;
    }

    public void setId(String id) {
        this.id = id;
        setLisetnerOnReportWithUserId();
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCancellationText(String cancellationText) {
        this.cancellationText = cancellationText;
    }

    @Exclude
    public void setIncrementalReportId() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child("reports").orderByChild("incrementalReportId").limitToLast(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Iterable<DataSnapshot> contactChildren = snapshot.getChildren();

                for (DataSnapshot report : contactChildren) {
                    Report lastReport = report.getValue(Report.class);
                    nextIncrementalId = lastReport.getIncrementalReportId() + 1;
                }
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });

        this.incrementalReportId = nextIncrementalId;
    }

    public void reportUpdateStatus(String status){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports").child(this.id).child("status");
        reportsRef.setValue(status);
    }

    public void reportUpdateExtraPhoneNumber(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports").child(this.id);
        Map<String,Object> reportMap = new HashMap<String,Object>();
        reportMap.put("extraPhoneNumber", this.extraPhoneNumber);
        reportsRef.updateChildren(reportMap);
    }

    public void reportUpdateIncrementalReportId(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports").child(this.id).child("incrementalReportId");
        reportsRef.setValue(this.incrementalReportId);
    }

    public void reportUpdateCancellationText(String cancellationText){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference reportsRef = ref.child("reports").child(this.id);
        Map<String,Object> reportMap = new HashMap<String,Object>();
        reportMap.put("cancellationText", cancellationText);
        reportsRef.updateChildren(reportMap);
    }

    public boolean isOpenReport(){
        if ((Objects.equals(this.status, "CANCELED")) || (Objects.equals(this.status, "CLOSED")))
                return false;
        else return true;
    }

    public String statusInHebrew(){
        Map<String, String> map = new HashMap<String, String>();
        map.put("NEW", "הדיווח בטיפול מנהל");
        map.put("CLOSED", "הטיפול בדיווח הסתיים");
        map.put("CANCELED", "הדיווח בוטל");
        map.put("SCANER_ON_THE_WAY", "סורק בדרך אליך");
        return map.get(this.status);
    }

    public String validate(boolean checked){
        String error ="";
        if(reporterName.equals("")){
            error = error+ "חסר שם ";
        }
        if(phoneNumber.equals("")){
            error = error+ "חסר מספר טלפון ";
        }else {
            if (!phoneNumber.matches("^([0-9]{10})|([0-9]{3}-[0-9]{7})|([0-9]{2}-[0-9]{7})$")) {
                error = error + "מספר טלפון לא תקין";
            }
        }
        if(address.equals("")){
            error = error+ "חסרה כתובת ";
        }
        if(!checked){
            error =error+ " צ'קבוקס לא מסומן ";
        }

        if(hasSimilarReports){
            error =error+ "בקשה על שימך כבר נמצאת במערכת";
        }
        return error;
    }



    @Override
    public String toString() {
        return "Report{" +
                "id='" + id + '\'' +
                ", reportyName='" + reporterName + '\'' +
                ", status='" + status + '\'' +
                ", startTime='" + startTime + '\'' +
                ", address='" + address + '\'' +
                ", imageUrls=" + Arrays.toString(imageUrls) +
                ", freeText='" + freeText + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", extraPhoneNumber='" + extraPhoneNumber + '\'' +
                ", assignedScanner='" + assignedScanner + '\'' +
                ", availableScanners=" + availableScanners +
                '}';
    }


    public Bitmap getBitmapFromURL (String src){
        try{
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (Exception e){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString();
            Log.w(TAG, sStackTrace);
            return null;
        }


    }
}