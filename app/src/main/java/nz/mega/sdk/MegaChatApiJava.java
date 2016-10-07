package nz.mega.sdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import nz.mega.sdk.MegaApiJava;

public class MegaChatApiJava {
    MegaChatApi megaChatApi;
    static DelegateMegaChatLogger logger;

    // Error information but application will continue run.
    public final static int LOG_LEVEL_ERROR = MegaChatApi.LOG_LEVEL_ERROR;
    // Information representing errors in application but application will keep running
    public final static int LOG_LEVEL_WARNING = MegaChatApi.LOG_LEVEL_WARNING;
    // Mainly useful to represent current progress of application.
    public final static int LOG_LEVEL_INFO = MegaChatApi.LOG_LEVEL_INFO;
    public final static int LOG_LEVEL_VERBOSE = MegaChatApi.LOG_LEVEL_VERBOSE;
    // Informational logs, that are useful for developers. Only applicable if DEBUG is defined.
    public final static int LOG_LEVEL_DEBUG = MegaChatApi.LOG_LEVEL_DEBUG;
    public final static int LOG_LEVEL_MAX = MegaChatApi.LOG_LEVEL_MAX;

    static Set<DelegateMegaChatRequestListener> activeRequestListeners = Collections.synchronizedSet(new LinkedHashSet<DelegateMegaChatRequestListener>());
    static Set<DelegateMegaChatListener> activeChatListeners = Collections.synchronizedSet(new LinkedHashSet<DelegateMegaChatListener>());
    static Set<DelegateMegaChatRoomListener> activeChatRoomListeners = Collections.synchronizedSet(new LinkedHashSet<DelegateMegaChatRoomListener>());

    void runCallback(Runnable runnable) {
        runnable.run();
    }

    /**
     * Creates an instance of MegaChatApi to access to the chat-engine.
     *
     * @param megaApi Instance of MegaApi to be used by the chat-engine.
     * session will be discarded and MegaChatApi expects to have a login+fetchnodes before MegaChatApi::init
     */
    public MegaChatApiJava(MegaApiJava megaApi){
        megaChatApi = new MegaChatApi(megaApi.getMegaApi());
    }

    public void addChatRequestListener(MegaChatRequestListenerInterface listener)
    {
        megaChatApi.addChatRequestListener(createDelegateRequestListener(listener, false));
    }

    public void addChatListener(MegaChatListenerInterface listener)
    {
        megaChatApi.addChatListener(createDelegateChatListener(listener));
    }

    public void init(boolean resumeSession)
    {
        megaChatApi.init(resumeSession);
    }

    public void init(boolean resumeSession, MegaChatRequestListenerInterface listener)
    {
        megaChatApi.init(resumeSession, createDelegateRequestListener(listener));
    }

    public void connect()
    {
        megaChatApi.connect();
    }

    public void connect(MegaChatRequestListenerInterface listener)
    {
        megaChatApi.connect(createDelegateRequestListener(listener));
    }

    public void setOnlineStatus(int status)
    {
        megaChatApi.setOnlineStatus(status);
    }

    public void setOnlineStatus(int status, MegaChatRequestListenerInterface listener)
    {
        megaChatApi.setOnlineStatus(status, createDelegateRequestListener(listener));
    }

    /**
     * Creates a chat for one or more participants, allowing you to specify their
     * permissions and if the chat should be a group chat or not (when it is just for 2 participants).
     *
     * There are two types of chat: permanent an group. A permanent chat is between two people, and
     * participants can not leave it.
     *
     * The creator of the chat will have moderator level privilege and should not be included in the
     * list of peers.
     *
     * The associated request type with this request is MegaChatRequest::TYPE_CREATE_CHATROOM
     * Valid data in the MegaChatRequest object received on callbacks:
     * - MegaChatRequest::getFlag - Returns if the new chat is a group chat or permanent chat
     * - MegaChatRequest::getMegaChatPeerList - List of participants and their privilege level
     *
     * Valid data in the MegaChatRequest object received in onRequestFinish when the error code
     * is MegaError::ERROR_OK:
     * - MegaChatRequest::getChatHandle - Returns the handle of the new chatroom
     *
     * @note If you are trying to create a chat with more than 1 other person, then it will be forced
     * to be a group chat.
     *
     * @note If peers list contains only one person, group chat is not set and a permament chat already
     * exists with that person, then this call will return the information for the existing chat, rather
     * than a new chat.
     *
     * @param group Flag to indicate if the chat is a group chat or not
     * @param peers MegaChatPeerList including other users and their privilege level
     * @param listener MegaChatRequestListener to track this request
     */
    public void createChat(boolean group, MegaChatPeerList peers, MegaChatRequestListenerInterface listener){
        megaChatApi.createChat(group, peers, createDelegateRequestListener(listener));
    }

    public void inviteToChat(long chatid, long userhandle, int privs)
    {
        megaChatApi.inviteToChat(chatid, userhandle, privs);
    }

    public void inviteToChat(long chatid, long userhandle, int privs, MegaChatRequestListenerInterface listener)
    {
        megaChatApi.inviteToChat(chatid, userhandle, privs, createDelegateRequestListener(listener));
    }

    public ArrayList<MegaChatRoom> getChatRooms()
    {
        return chatRoomListToArray(megaChatApi.getChatRooms());
    }

    /**
     * Get the MegaChatRoom for the 1on1 chat with the specified user
     *
     * If the 1on1 chat with the user specified doesn't exist, this function will
     * return NULL.
     *
     * It is needed to have successfully completed the \c MegaChatApi::init request
     * before calling this function.
     *
     * You take the ownership of the returned value
     *
     * @param userhandle MegaChatHandle that identifies the user
     * @return MegaChatRoom object for the specified \c userhandle
     */
    public MegaChatRoom getChatRoomByUser(long userhandle){
        return megaChatApi.getChatRoomByUser(userhandle);
    }

    /**
     * Get the MegaChatRoom that has a specific handle
     *
     * You can get the handle of a MegaChatRoom using MegaChatRoom::getChatId or
     * MegaChatListItem::getChatId.
     *
     * It is needed to have successfully completed the \c MegaChatApi::init request
     * before calling this function.
     *
     * You take the ownership of the returned value
     *
     * @return List of MegaChatRoom objects with all chatrooms of this account.
     */
    public MegaChatRoom getChatRoom(long chatid){
        return megaChatApi.getChatRoom(chatid);
    }

/*
    /**
     * @brief Returns the handle of the user.
     *
     * @return For outgoing messages, it returns the handle of the target user.
     * For incoming messages, it returns the handle of the sender.
     *
    public long getUserHandle()
    {

    }
*/

    public void removeFromChat(long chatid, long userhandle)
    {
        megaChatApi.removeFromChat(chatid, userhandle);
    }

    public void removeFromChat(long chatid, long userhandle, MegaChatRequestListenerInterface listener)
    {
        megaChatApi.removeFromChat(chatid, userhandle, createDelegateRequestListener(listener));
    }

    public void updateChatPermissions(long chatid, long userhandle, int privilege)
    {
        megaChatApi.updateChatPermissions(chatid, userhandle, privilege);
    }

    public void updateChatPermissions(long chatid, long userhandle, int privilege, MegaChatRequestListenerInterface listener)
    {
        megaChatApi.updateChatPermissions(chatid, userhandle, privilege, createDelegateRequestListener(listener));
    }

    public void setChatTitle(long chatid, String title)
    {
        megaChatApi.setChatTitle(chatid, title);
    }

    public void setChatTitle(long chatid, String title, MegaChatRequestListenerInterface listener)
    {
        megaChatApi.setChatTitle(chatid, title, createDelegateRequestListener(listener));
    }

    public void truncateChat(long chatid, long messageid)
    {
        megaChatApi.truncateChat(chatid, messageid);
    }

    public void truncateChat(long chatid, long messageid, MegaChatRequestListenerInterface listener)
    {
        megaChatApi.truncateChat(chatid, messageid, createDelegateRequestListener(listener));
    }

    /**
     * This method should be called when a chat is opened
     *
     * The second parameter is the listener that will receive notifications about
     * events related to the specified chatroom.
     *
     * @param chatid MegaChatHandle that identifies the chat room
     * @param listener MegaChatRoomListener to track events on this chatroom
     *
     * @return True if success, false if the chatroom was not found.
     */
//    public boolean openChatRoom(long chatid, MegaChatRoomListenerInterface listener){
    public boolean openChatRoom(long chatid, MegaChatRoomListenerInterface listener){
        return megaChatApi.openChatRoom(chatid, createDelegateChatRoomListener(listener));
    }

    /**
     * This method should be called when a chat is closed.
     *
     * It automatically unregisters the listener to stop receiving the related events.
     *
     * @param chatid MegaChatHandle that identifies the chat room
     * @param listener MegaChatRoomListener to be unregistered.
     */
    public void closeChatRoom(long chatid, MegaChatRoomListenerInterface listener){
        megaChatApi.openChatRoom(chatid, createDelegateChatRoomListener(listener));
    }

    /**
     * Initiates fetching more history of the specified chatroom.
     *
     * The loaded messages will be notified one by one through the MegaChatRoomListener
     * specified at MegaChatApi::openChatRoom (and through any other listener you may have
     * registered by calling MegaChatApi::addChatRoomListener).
     *
     * The corresponding callback is MegaChatRoomListener::onMessageLoaded.
     *
     * @note The actual number of messages loaded can be less than \c count. One reason is
     * the history being shorter than requested, the other is due to internal protocol
     * messages that are not intended to be displayed to the user. Additionally, if the fetch
     * is local and there's no more history locally available, the number of messages could be
     * lower too (and the next call to MegaChatApi::getMessages will fetch messages from server).
     *
     * @param chatid MegaChatHandle that identifies the chat room
     * @param count The number of requested messages to load.
     *
     * @return True if the fetch is local, false if it will request the server. This value
     * can be used to show a progress bar accordingly when network operation occurs.
     */
    public boolean getMessages(long chatid, int count){
        return megaChatApi.getMessages(chatid, count);
    }

    /**
     * Returns the last-seen-by-us message
     *
     * @param chatid MegaChatHandle that identifies the chat room
     *
     * @return The last-seen-by-us MegaChatMessage, or NULL if error.
     */
    public MegaChatMessage getLastMessageSeen(long chatid){
        return megaChatApi.getLastMessageSeen(chatid);
    }

    /**
     * Set the active log level.
     * <p>
     * This function sets the log level of the logging system. If you set a log listener using
     * MegaApiJava.setLoggerObject(), you will receive logs with the same or a lower level than
     * the one passed to this function.
     *
     * @param logLevel
     *            Active log level. These are the valid values for this parameter: <br>
     *                Valid values are:
     * - MegaChatApi::LOG_LEVEL_ERROR   = 1
     * - MegaChatApi::LOG_LEVEL_WARNING = 2
     * - MegaChatApi::LOG_LEVEL_INFO    = 3
     * - MegaChatApi::LOG_LEVEL_VERBOSE = 4
     * - MegaChatApi::LOG_LEVEL_DEBUG   = 5
     * - MegaChatApi::LOG_LEVEL_MAX     = 6
     *            - MegaApiJava.LOG_LEVEL_FATAL = 0. <br>
     *            - MegaApiJava.LOG_LEVEL_ERROR = 1. <br>
     *            - MegaApiJava.LOG_LEVEL_WARNING = 2. <br>
     *            - MegaApiJava.LOG_LEVEL_INFO = 3. <br>
     *            - MegaApiJava.LOG_LEVEL_DEBUG = 4. <br>
     *            - MegaApiJava.LOG_LEVEL_MAX = 5.
     */
    public static void setLogLevel(int logLevel) {
        MegaChatApi.setLogLevel(logLevel);
    }

    /**
     * Set a MegaLogger implementation to receive SDK logs.
     * <p>
     * Logs received by this objects depends on the active log level.
     * By default, it is MegaApiJava.LOG_LEVEL_INFO. You can change it
     * using MegaApiJava.setLogLevel().
     *
     * @param megaLogger
     *            MegaChatLogger implementation.
     */
    public static void setLoggerObject(MegaChatLoggerInterface megaLogger) {
        DelegateMegaChatLogger newLogger = new DelegateMegaChatLogger(megaLogger);
        MegaChatApi.setLoggerObject(newLogger);
        logger = newLogger;
    }

    private MegaChatRequestListener createDelegateRequestListener(MegaChatRequestListenerInterface listener) {
        DelegateMegaChatRequestListener delegateListener = new DelegateMegaChatRequestListener(this, listener, true);
        activeRequestListeners.add(delegateListener);
        return delegateListener;
    }

    private MegaChatRequestListener createDelegateRequestListener(MegaChatRequestListenerInterface listener, boolean singleListener) {
        DelegateMegaChatRequestListener delegateListener = new DelegateMegaChatRequestListener(this, listener, singleListener);
        activeRequestListeners.add(delegateListener);
        return delegateListener;
    }

    private MegaChatRoomListener createDelegateChatRoomListener(MegaChatRoomListenerInterface listener) {
        DelegateMegaChatRoomListener delegateListener = new DelegateMegaChatRoomListener(this, listener);
        activeChatRoomListeners.add(delegateListener);
        return delegateListener;
    }

    private MegaChatListener createDelegateChatListener(MegaChatListenerInterface listener) {
        DelegateMegaChatListener delegateListener = new DelegateMegaChatListener(this, listener);
        activeChatListeners.add(delegateListener);
        return delegateListener;
    }

    void privateFreeRequestListener(DelegateMegaChatRequestListener listener) {
        activeRequestListeners.remove(listener);
    }

    static ArrayList<MegaChatRoom> chatRoomListToArray(MegaChatRoomList chatRoomList) {

        if (chatRoomList == null) {
            return null;
        }

        ArrayList<MegaChatRoom> result = new ArrayList<MegaChatRoom>((int)chatRoomList.size());
        for (int i = 0; i < chatRoomList.size(); i++) {
            result.add(chatRoomList.get(i).copy());
        }

        return result;
    }
};
