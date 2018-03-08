package mega.privacy.android.app.lollipop.megachat;

import java.util.ArrayList;

public class PendingMessage {

    public static int STATE_SENDING = 0;
    public static int STATE_SENT = 1;
    public static int STATE_ERROR = -1;

    long chatId;
    ArrayList<PendingNodeAttachment> nodeAttachments = new ArrayList<>();
    long id;
    long uploadTimestamp;
    int state;
    String videoDownSampled;

    public PendingMessage(long id, long chatId, ArrayList<String> filePaths, int state) {
        this.id = id;
        this.chatId = chatId;
        this.state = state;
        for(int i=0;i<filePaths.size();i++){
            PendingNodeAttachment nodeAttachment = new PendingNodeAttachment(filePaths.get(i));
            nodeAttachments.add(nodeAttachment);
        }
    }

    public PendingMessage(long id, long chatId, ArrayList<String> filePaths, ArrayList<String> fingerprints, ArrayList<String> names, long uploadTimestamp) {
        this.chatId = chatId;
        this.id = id;
        this.uploadTimestamp = uploadTimestamp;

        for(int i=0;i<filePaths.size();i++){
            PendingNodeAttachment nodeAttachment = new PendingNodeAttachment(filePaths.get(i), fingerprints.get(i), names.get(i));
            nodeAttachments.add(nodeAttachment);
        }
    }

    public PendingMessage(long id, long chatId, ArrayList<PendingNodeAttachment> nodeAttachments, long uploadTimestamp, int state) {
        this.chatId = chatId;
        this.nodeAttachments = nodeAttachments;
        this.id = id;
        this.uploadTimestamp = uploadTimestamp;
        this.state = state;
    }

    public PendingMessage(long id, long chatId, long uploadTimestamp) {
        this.chatId = chatId;
        this.id = id;
        this.uploadTimestamp = uploadTimestamp;
    }

    public ArrayList<PendingNodeAttachment> getNodeAttachments() {
        return nodeAttachments;
    }

    public void setNodeAttachments(ArrayList<PendingNodeAttachment> nodeAttachments) {
        this.nodeAttachments = nodeAttachments;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(long uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }

    public boolean isNodeHandlesCompleted(){
        for(int i=0;i<nodeAttachments.size();i++){
            if(nodeAttachments.get(i).getNodeHandle()==-1){
                return false;
            }
        }
        return true;
    }

    public ArrayList<Long> getNodeHandles(){
        ArrayList<Long> nodeHandles = new ArrayList<>();
        for(int i=0;i<nodeAttachments.size();i++){
            nodeHandles.add(nodeAttachments.get(i).getNodeHandle());
        }
        return nodeHandles;
    }

    public ArrayList<String> getFilePaths(){
        ArrayList<String> filePaths = new ArrayList<>();
        for(int i=0;i<nodeAttachments.size();i++){
            filePaths.add(nodeAttachments.get(i).getFilePath());
        }
        return filePaths;
    }

    public ArrayList<String> getNames(){
        ArrayList<String> names = new ArrayList<>();
        for(int i=0;i<nodeAttachments.size();i++){
            names.add(nodeAttachments.get(i).getName());
        }
        return names;
    }

    public String getVideoDownSampled() {
        return videoDownSampled;
    }

    public void setVideoDownSampled(String videoDownSampled) {
        this.videoDownSampled = videoDownSampled;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
