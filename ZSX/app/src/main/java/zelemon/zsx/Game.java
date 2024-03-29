package zelemon.zsx;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.renderscript.Int2;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.GamesClientStatusCodes;
import com.google.android.gms.games.InvitationsClient;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationCallback;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.apache.commons.lang3.SerializationUtils;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;
import zelemon.zsx.battery.BatteryLiveData;
import zelemon.zsx.battery.StatusActivity;
import zelemon.zsx.dependencyInjection.TronViewModelFactory;
import zelemon.zsx.persistence.database.PictureTypeConverter;
import zelemon.zsx.persistence.database.Profile;
import zelemon.zsx.persistence.database.SerializableProfile;


/**
 * Button Clicker 2000. A minimalistic game showing the multiplayer features of
 * the Google Play game services API. The objective of this game is clicking a
 * button. Whoever clicks the button the most times within a 20 second interval
 * wins. It's that simple. This game can be played with 2, 3 or 4 players. The
 * code is organized in sections in order to make understanding as clear as
 * possible. We start with the integration section where we show how the game
 * is integrated with the Google Play game services API, then move on to
 * game-specific UI and logic.
 * <p>
 * INSTRUCTIONS: To run this sample, please set up
 * a project in the Developer Console. Then, place your app ID on
 * res/values/ids.xml. Also, change the package name to the package name you
 * used to create the client ID in Developer Console. Make sure you sign the
 * APK with the certificate whose fingerprint you entered in Developer Console
 * when creating your Client Id.
 *
 * @author Bruno Oliveira (btco), 2013-04-26
 */
public class Game extends DaggerAppCompatActivity implements
        View.OnClickListener {
    // This array lists everything that's clickable, so we can install click
    // event handlers.
    final static int[] CLICKABLES = {
            R.id.button_accept_popup_invitation, /*R.id.button_invite_players,*/
            R.id.button_quick_game, /*R.id.button_see_invitations,*/ R.id.button_sign_in,
            R.id.button_sign_out, R.id.button_see_map, R.id.button_see_profile, R.id.battery_viewer
    };
    final static String TAG = "ZSX";
    // Request codes for the UIs that we show with startActivityForResult:
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_INVITATION_INBOX = 10001;
    final static int RC_WAITING_ROOM = 10002;
    // Grid size
    final static Int2 GRID_SIZE = new Int2(90, 132);

    /*
     * API INTEGRATION SECTION. This section contains the code that integrates
     * the game with the Google Play game services API.
     */
    final static int QuarterX = GRID_SIZE.x / 4;
    final static int MidY = GRID_SIZE.y / 2;
    final static int ThreeQuarterX = 3 * GRID_SIZE.x / 4;
    // This array lists all the individual screens our game has.
    final static int[] SCREENS = {
            R.id.screen_game, R.id.screen_main, R.id.screen_sign_in,
            R.id.screen_wait
    };
    final static int[] PLAYER_LIVES = {
            R.id.life3, R.id.life2, R.id.life1
    };
    final static int[] ENEMY_LIVES = {
            R.id.enemy_life3, R.id.enemy_life2, R.id.enemy_life1
    };
    private static final int PERMISSION_REQUEST_LOCATION = 0;
    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;
    private static boolean isStartupLaunch = true;
    protected int playerColor = Color.CYAN;
    // Room ID where the currently active game is taking place; null if we're
    // not playing.
    String mRoomId = null;
    // Holds the configuration of the current room.
    RoomConfig mRoomConfig;
    // Are we playing in multiplayer mode?
    boolean mMultiplayer = false;
    // The participants in the currently active game
    ArrayList<Participant> mParticipants = null;
    // My participant ID in the currently active game
    String mMyId = null;
    // If non-null, this is the id of the invitation we received via the
    // invitation listener
    String mIncomingInvitationId = null;
    // Message buffer for sending messages
    byte[] mMsgBuf = new byte[100];
    byte[] msgCollBuf = new byte[100];
    // The currently signed in account, used to check the account has changed outside of this activity when resuming.
    GoogleSignInAccount mSignedInAccount = null;
    // Current state of the game:
    int mSecondsLeft = -1; // how long until the game ends (seconds)
    int mScore = 0; // user's current score
    // Score of other participants. We update this as we receive their scores
    // from the network.
    Map<String, Integer> mParticipantLives = new HashMap<>();
    Map<String, Enemy> mParticipantEnemy = new HashMap<>();
    // Participants who sent us their final score.
    Set<String> mFinishedParticipants = new HashSet<>();
    int mCurScreen = -1;
    @Inject
    TronViewModelFactory viewModelFactory;
    SurfaceView mSurfaceView = null;
    private boolean isRestarting;
    private TextView mStartGameCountdown;
    private ArrayList<ImageView> mPLayerLives = new ArrayList<>();
    private ArrayList<ImageView> mEnemyLives = new ArrayList<>();
    private boolean gameReady;
    // Client used to sign in with Google APIs
    private GoogleSignInClient mGoogleSignInClient = null;
    // Client used to interact with the real time multiplayer system.
    private RealTimeMultiplayerClient mRealTimeMultiplayerClient = null;
    // Client used to interact with the Invitation system.
    private InvitationsClient mInvitationsClient = null;
    // Handle the result of the "Select players UI" we launched when the user clicked the
    // "Invite friends" button. We react by creating a room with those players.
    private String mPlayerId;
    private int mParticipantIndex;
    private GamePanel mGamePanel;
    private boolean isWaitingCollisionAck;
    private ImageView backgroundImage;
    private TextView mLostOverlay;
    private boolean mIsGameFinished;
    private TextView mWonOverlay;
    private TronViewModel tronViewModel;
    private Profile mProfile;
    private Location currentLocation;
    private final Observer<Location> locationObserver = location -> currentLocation = location;
    private ConstraintLayout mGameScreen;
    private LinearLayout mFinishedGameButton;
    /*
     * CALLBACKS SECTION. This section shows how we implement the several games
     * API callbacks.
     */
    private MediaPlayer media;
    private CountDownTimer mWifiTimeout = null;
    private ViewGroup.LayoutParams mGamePanelParams;
    // Called when we receive a real-time message from the network.
    // Messages in our game are made up of 2 bytes: the first one is 'F' or 'U'
    // indicating
    // whether it's a final or interim score. The second byte is the score.
    // There is also the
    // 'S' message, which indicates that the game should start.
    OnRealTimeMessageReceivedListener mOnRealTimeMessageReceivedListener = new OnRealTimeMessageReceivedListener() {
        @Override
        public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
            if (mWifiTimeout != null) {
                mWifiTimeout.cancel();
                mWifiTimeout.start();
            }
            byte[] buf = realTimeMessage.getMessageData();
            String sender = realTimeMessage.getSenderParticipantId();

            if (buf[0] == 'C' && !isRestarting) {
                Log.i("COMM", "COLLISION message received");
                Log.i("COMM", "\tfrom " + sender);
                mFinishedParticipants.add(sender);
                mParticipantLives.put(sender, mParticipantLives.get(sender) - 1);

                mGamePanel.stopGameUpdate();
                sendCollisionAck(sender);
//                Handler handler = new Handler();
//                handler.postDelayed(new DelayedAck(sender), 1000);

            } else if (buf[0] == 'A' && !isRestarting) {
                Log.i("COMM", "ACK COLLISION message received");
                Log.i("COMM", "\tfrom " + sender);
                isWaitingCollisionAck = false;
                mFinishedParticipants.add(sender);
                checkCollisionResolve();
            } else if (buf[0] == 'P') {
                // Position update
                if (mParticipantEnemy.containsKey(sender)) {
                    byte[] positionX = Arrays.copyOfRange(buf, 1, 5);
                    byte[] positionY = Arrays.copyOfRange(buf, 5, 9);
                    mParticipantEnemy.get(sender).setEnemyPosition(new Int2(parseIntFromByteArray(positionX), parseIntFromByteArray(positionY)));
                }
                StringBuilder sb = new StringBuilder();
            }
            // Initialize Game
            else if (buf[0] == 'I') {
                Log.i("COMM", "INITIALIZE message received");
                Log.i("COMM", "\tfrom " + sender);
                for (int i = 0; i < mParticipants.size(); i++) {
                    Participant participant = mParticipants.get(i);
                    if (i == mParticipantIndex) {
                        playerColor = parseIntFromByteArray(Arrays.copyOfRange(buf, 4 * i + 1, 4 * i + 5));
                    } else {
                        Log.v("ZSX", "Creating Enemy at initialization");
                        mParticipantEnemy.put(participant.getParticipantId(), new Enemy(parseIntFromByteArray(Arrays.copyOfRange(buf, 4 * i + 1, 4 * i + 5)), getInitialPosition(i), GRID_SIZE));
                    }
                }
                if (mWifiTimeout == null) {
                    // Start wifi lost timeout
                    mWifiTimeout = new CountDownTimer(5000, 50) {
                        public void onTick(long millisUntilFinished) {
                        }

                        public void onFinish() {
                            Log.v("ZSX", "WIFI timeout");
                            Toast.makeText(getApplicationContext(), "Connection to opponent was lost", Toast.LENGTH_SHORT).show();
                            resetGameVars();
                            leaveRoom();
                        }
                    }.start();
                }
            }
            // Start Game
            else if (buf[0] == 'S') {
                StringBuilder sb = new StringBuilder();
                sb.append("START message received\n\tfrom ");
                sb.append(sender);
                Log.i("COMM", sb.toString());
                startGame(mMultiplayer);
            }
            // Player info
            else if (buf[0] == 'M') {
                byte[] data = new byte[buf.length - 1];
                System.arraycopy(buf, 1, data, 0, data.length);
                SerializableProfile profile = SerializationUtils.deserialize(data);
                tronViewModel.saveProfile(new Profile(profile.getName(), profile.getLocation(), profile.getPicture()));
            }
        }
    };
    private InvitationCallback mInvitationCallback = new InvitationCallback() {
        // Called when we get an invitation to play a game. We react by showing that to the user.
        @Override
        public void onInvitationReceived(@NonNull Invitation invitation) {
            // We got an invitation to play a game! So, store it in
            // mIncomingInvitationId
            // and show the popup on the screen.
            mIncomingInvitationId = invitation.getInvitationId();
            ((TextView) findViewById(R.id.incoming_invitation_text)).setText(
                    invitation.getInviter().getDisplayName() + " " +
                            getString(R.string.is_inviting_you));
            switchToScreen(mCurScreen); // This will show the invitation popup
        }

        @Override
        public void onInvitationRemoved(@NonNull String invitationId) {

            if (mIncomingInvitationId.equals(invitationId) && mIncomingInvitationId != null) {
                mIncomingInvitationId = null;
                switchToScreen(mCurScreen); // This will hide the invitation popup
            }
        }
    };
    private RoomStatusUpdateCallback mRoomStatusUpdateCallback = new RoomStatusUpdateCallback() {
        // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
        // is connected yet).
        @Override
        public void onConnectedToRoom(Room room) {
            Log.d(TAG, "onConnectedToRoom.");

            //get participants and my ID:
            mParticipants = room.getParticipants();
            mMyId = room.getParticipantId(mPlayerId);

            // save room ID if its not initialized in onRoomCreated() so we can leave cleanly before the game starts.
            if (mRoomId == null) {
                mRoomId = room.getRoomId();
            }

            // print out the list of participants (for debug purposes)
            Log.d(TAG, "Room ID: " + mRoomId);
            Log.d(TAG, "My ID " + mMyId);
            Log.d(TAG, "<< CONNECTED TO ROOM>>");
        }

        // Called when we get disconnected from the room. We return to the main screen.
        @Override
        public void onDisconnectedFromRoom(Room room) {
            mRoomId = null;
            mRoomConfig = null;
            showGameError();
        }


        // We treat most of the room update callbacks in the same way: we update our list of
        // participants and update the display. In a real game we would also have to check if that
        // change requires some action like removing the corresponding player avatar from the screen,
        // etc.
        @Override
        public void onPeerDeclined(Room room, @NonNull List<String> arg1) {
            updateRoom(room);
        }

        @Override
        public void onPeerInvitedToRoom(Room room, @NonNull List<String> arg1) {
            updateRoom(room);
        }

        @Override
        public void onP2PDisconnected(@NonNull String participant) {
        }

        @Override
        public void onP2PConnected(@NonNull String participant) {
        }

        @Override
        public void onPeerJoined(Room room, @NonNull List<String> arg1) {
            updateRoom(room);
        }

        @Override
        public void onPeerLeft(Room room, @NonNull List<String> peersWhoLeft) {
            updateRoom(room);
        }

        @Override
        public void onRoomAutoMatching(Room room) {
            updateRoom(room);
        }

        @Override
        public void onRoomConnecting(Room room) {
            updateRoom(room);
        }

        @Override
        public void onPeersConnected(Room room, @NonNull List<String> peers) {
            updateRoom(room);
        }

        @Override
        public void onPeersDisconnected(Room room, @NonNull List<String> peers) {
            updateRoom(room);
        }
    };
    private RoomUpdateCallback mRoomUpdateCallback = new RoomUpdateCallback() {

        // Called when room has been created
        @Override
        public void onRoomCreated(int statusCode, Room room) {
            Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
                showGameError();
                return;
            }

            // save room ID so we can leave cleanly before the game starts.
            mRoomId = room.getRoomId();

            // show the waiting room UI
            showWaitingRoom(room);
        }

        // Called when room is fully connected.
        @Override
        public void onRoomConnected(int statusCode, Room room) {
            Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
                showGameError();
                return;
            }
            updateRoom(room);
        }

        @Override
        public void onJoinedRoom(int statusCode, Room room) {
            Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
                showGameError();
                return;
            }

            // show the waiting room UI
            showWaitingRoom(room);
        }

        // Called when we've successfully left the room (this happens a result of voluntarily leaving
        // via a call to leaveRoom(). If we get disconnected, we get onDisconnectedFromRoom()).
        @Override
        public void onLeftRoom(int statusCode, @NonNull String roomId) {
            // we have left the room; return to main screen.
            Log.d(TAG, "onLeftRoom, code " + statusCode);
            switchToMainScreen();
        }
    };

    public static byte[] intToByteArray(int anInteger) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(anInteger).array();
    }

    public static int parseIntFromByteArray(byte[] byteBarray) {
        if (byteBarray.length != 4) {
            StringBuilder sb = new StringBuilder();
            sb.append("Error converting byte array of size ");
            sb.append(byteBarray.length);
            sb.append(" to int");
            Log.e("ZSX", sb.toString());
        }
        return ByteBuffer.wrap(byteBarray).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public static byte[] floatToByteArray(float value) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(value).array();
    }

    public static float parseFloatFromByteArray(byte[] byteBarray) {
        if (byteBarray.length != 4) {
            StringBuilder sb = new StringBuilder();
            sb.append("Error converting byte array of size ");
            sb.append(byteBarray.length);
            sb.append(" to float");
            Log.e("ZSX", sb.toString());
        }
        return ByteBuffer.wrap(byteBarray).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }

    // Source: https://stackoverflow.com/questions/8911356/whats-the-best-practice-to-round-a-float-to-2-decimals
    public static BigDecimal round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd;
    }

    private void checkCollisionResolve() {
        if (mFinishedParticipants.size() == mParticipants.size()) {
            for (Participant participant : mParticipants) {
                // If either player is dead restart game
                if (mParticipantLives.get(participant.getParticipantId()) <= 0) {
                    if (participant.getParticipantId().equals(mParticipants.get(mParticipantIndex).getParticipantId())) {
                        mLostOverlay.setVisibility(View.VISIBLE);
                        Log.v("ZSX", "You lost!" + mParticipants.get(mParticipantIndex).getParticipantId());
                    }
                    mIsGameFinished = true;
                }
            }
            if (mIsGameFinished) {
                if (mWifiTimeout != null) {
                    mWifiTimeout.cancel();
                }
                if (mLostOverlay.getVisibility() == View.GONE) {
                    mWonOverlay.setVisibility(View.VISIBLE);
                    Log.v("ZSX", "You won!" + mParticipants.get(mParticipantIndex).getParticipantId());
                }
                Log.v("ZSX", "Game finished" + mParticipants.get(mParticipantIndex).getParticipantId());
                return;
            }
            restartGame();
        } else {
            Log.v("COMM", "Collision resolve failed for " + mParticipants.get(mParticipantIndex).getParticipantId());
        }
    }

    private Int2 getInitialPosition(int i) {
        Int2 pos;
        if (mParticipants.size() <= 2) {
            if (i == 0) {
                pos = new Int2(QuarterX, MidY);
            } else {
                pos = new Int2(ThreeQuarterX, MidY);
            }
        }
//        else if (nbParticipants <= 4)
//            return THREE_PLUS_PLAYER_START[i];
        else
            throw new RuntimeException("Number of participant above 4");
        return pos;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_LOCATION:
                if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    signInSilently();
                }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Runs only once at startup
        if (isStartupLaunch) {
            BatteryLiveData.InitializeBatteryStatus(getApplicationContext());
            isStartupLaunch = false;
        }

        setContentView(R.layout.activity_main);


        mSurfaceView = findViewById(R.id.surface);


        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                media = MediaPlayer.create(getApplicationContext(), R.raw.background_vid);

                media.setDisplay(mSurfaceView.getHolder());

                media.setLooping(true);
                media.start();

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
//                if (media != null) {
//                    media.stop();
//                    media.release();
//                    media = null;
//                }
            }
        });


        // Get view model
        tronViewModel = ViewModelProviders.of(this, viewModelFactory).get(TronViewModel.class);
        // Create the client used to sign in.
        mGoogleSignInClient = GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).requestProfile().build());
        // set up a click listener for everything we care about
        for (int id : CLICKABLES) {
            findViewById(id).setOnClickListener(this);
        }
        for (int id : PLAYER_LIVES) {
            mPLayerLives.add(findViewById(id));
        }
        for (int id : ENEMY_LIVES) {
            mEnemyLives.add(findViewById(id));
        }
        backgroundImage = findViewById(R.id.screen_game_background);
        mLostOverlay = findViewById(R.id.game_lost_overlay);
        mWonOverlay = findViewById(R.id.game_won_overlay);

        mLostOverlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                leaveGameScreen(arg0);
                return true;
            }
        });
        mWonOverlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                leaveGameScreen(arg0);
                return true;
            }
        });

        switchToMainScreen();
        checkPlaceholderIds();
    }

    // Check the sample to ensure all placeholder ids are are updated with real-world values.
    // This is strictly for the purpose of the samples; you don't need this in a production
    // application.
    private void checkPlaceholderIds() {
        StringBuilder problems = new StringBuilder();

        if (getPackageName().startsWith("com.google.")) {
            problems.append("- Package name start with com.google.*\n");
        }

        for (Integer id : new Integer[]{R.string.app_id}) {

            String value = getString(id);

            if (value.startsWith("YOUR_")) {
                // needs replacing
                problems.append("- Placeholders(YOUR_*) in ids.xml need updating\n");
                break;
            }
        }

        if (problems.length() > 0) {
            problems.insert(0, "The following problems were found:\n\n");

            problems.append("\nThese problems may prevent the app from working properly.");
            problems.append("\n\nSee the TODO window in Android Studio for more information");
            (new AlertDialog.Builder(this)).setMessage(problems.toString())
                    .setNeutralButton(android.R.string.ok, null).create().show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        // Since the state of the signed in user can change when the activity is not active
        // it is recommended to try and sign in silently from when the app resumes.

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
        } else {
            signInSilently();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // unregister our listeners.  They will be re-registered via onResume->signInSilently->onConnected.
        if (mInvitationsClient != null) {
            mInvitationsClient.unregisterInvitationCallback(mInvitationCallback);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_sign_in:
                // start the sign-in flow
                Log.d(TAG, "Sign-in button clicked");
                startSignInIntent();
                break;
            case R.id.button_sign_out:
                // user wants to sign out
                // sign out.
                Log.d(TAG, "Sign-out button clicked");
                signOut();
                switchToScreen(R.id.screen_sign_in);
                break;
            /*case R.id.button_invite_players:
                switchToScreen(R.id.screen_wait);

                // show list of invitable players
                mRealTimeMultiplayerClient.getSelectOpponentsIntent(1, 3).addOnSuccessListener(
                        new OnSuccessListener<Intent>() {
                            @Override
                            public void onSuccess(Intent intent) {
                                startActivityForResult(intent, RC_SELECT_PLAYERS);
                            }
                        }
                ).addOnFailureListener(createFailureListener("There was a problem selecting opponents."));
                break;
            case R.id.button_see_invitations:
                switchToScreen(R.id.screen_wait);

                // show list of pending invitations
                mInvitationsClient.getInvitationInboxIntent().addOnSuccessListener(
                        new OnSuccessListener<Intent>() {
                            @Override
                            public void onSuccess(Intent intent) {
                                startActivityForResult(intent, RC_INVITATION_INBOX);
                            }
                        }
                ).addOnFailureListener(createFailureListener("There was a problem getting the inbox."));
                break;*/
            case R.id.button_accept_popup_invitation:
                // user wants to accept the invitation shown on the invitation popup
                // (the one we got through the OnInvitationReceivedListener).
                acceptInviteToRoom(mIncomingInvitationId);
                mIncomingInvitationId = null;
                break;
            case R.id.button_quick_game:
                // user wants to play against a random opponent right now
                startQuickGame();
                break;
            case R.id.button_see_map:
                //switchToScreen(R.id.screen_wait);
                Intent mapIntent = new Intent(this, MapsActivity.class);
                mapIntent.putExtra("signedInAccount", mSignedInAccount);
                startActivity(mapIntent);
                break;
            case R.id.button_see_profile:
                //switchToScreen(R.id.screen_wait);
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                profileIntent.putExtra("signedInAccount", mSignedInAccount);
                startActivity(profileIntent);
                break;
            case R.id.battery_viewer:
                Intent batteryIntent = new Intent(this, StatusActivity.class);
                startActivity(batteryIntent);
                break;
        }
    }

    void startQuickGame() {
        // quick-start a game with 1 randomly selected opponent
        mSurfaceView.setVisibility(View.INVISIBLE);
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 2;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);
        switchToScreen(R.id.screen_wait);
        keepScreenOn();
        resetGameVars();

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();
        mRealTimeMultiplayerClient.create(mRoomConfig);
    }

    /**
     * Start a sign in activity.  To properly handle the result, call tryHandleSignInResult from
     * your Activity's onActivityResult function
     */
    public void startSignInIntent() {
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    /**
     * Try to sign in without displaying dialogs to the user.
     * <p>
     * If the user has already signed in previously, it will not show dialog.
     */
    public void signInSilently() {
        Log.d(TAG, "signInSilently()");

        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInSilently(): success");
                            onConnected(task.getResult());

                            String name = mSignedInAccount.getDisplayName();
                            if (name == null) {
                                Log.d("ERR", "Can't get profile display name.");
                            } else {
                                tronViewModel.getProfile(name).observeForever(profile -> mProfile = profile);
                                tronViewModel.getLocationLiveData().observeForever(locationObserver);
                            }


                        } else {
                            Log.d(TAG, "signInSilently(): failure", task.getException());
                            onDisconnected();
                        }
                    }
                });
    }

    public void signOut() {
        Log.d(TAG, "signOut()");

        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Log.d(TAG, "signOut(): success");
                        } else {
                            handleException(task.getException(), "signOut() failed!");
                        }

                        onDisconnected();
                    }
                });
    }

    /*
     * GAME LOGIC SECTION. Methods that implement the game's rules.
     */

    /**
     * Since a lot of the operations use tasks, we can use a common handler for whenever one fails.
     *
     * @param exception The exception to evaluate.  Will try to display a more descriptive reason for the exception.
     * @param details   Will display alongside the exception if you wish to provide more details for why the exception
     *                  happened
     */
    private void handleException(Exception exception, String details) {
        int status = 0;

        if (exception instanceof ApiException) {
            ApiException apiException = (ApiException) exception;
            status = apiException.getStatusCode();
        }

        String errorString = null;
        switch (status) {
            case GamesCallbackStatusCodes.OK:
                break;
            case GamesClientStatusCodes.MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
                errorString = getString(R.string.status_multiplayer_error_not_trusted_tester);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_ALREADY_REMATCHED:
                errorString = getString(R.string.match_error_already_rematched);
                break;
            case GamesClientStatusCodes.NETWORK_ERROR_OPERATION_FAILED:
                errorString = getString(R.string.network_error_operation_failed);
                break;
            case GamesClientStatusCodes.INTERNAL_ERROR:
                errorString = getString(R.string.internal_error);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_INACTIVE_MATCH:
                errorString = getString(R.string.match_error_inactive_match);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_LOCALLY_MODIFIED:
                errorString = getString(R.string.match_error_locally_modified);
                break;
            default:
                errorString = getString(R.string.unexpected_status, GamesClientStatusCodes.getStatusCodeString(status));
                break;
        }

        if (errorString == null) {
            return;
        }

        String message = getString(R.string.status_exception_error, details, status, exception);

        new AlertDialog.Builder(Game.this)
                .setTitle("Error")
                .setMessage(message + "\n" + errorString)
                .setNeutralButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(intent);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                onConnected(account);
            } catch (ApiException apiException) {
                String message = apiException.getMessage();
                if (message == null || message.isEmpty()) {
                    message = getString(R.string.signin_other_error);
                }

                onDisconnected();

                new AlertDialog.Builder(this)
                        .setMessage(message)
                        .setNeutralButton(android.R.string.ok, null)
                        .show();
            }
        } else if (requestCode == RC_SELECT_PLAYERS) {
            // we got the result from the "select players" UI -- ready to create the room
            handleSelectPlayersResult(resultCode, intent);

        } else if (requestCode == RC_INVITATION_INBOX) {
            // we got the result from the "select invitation" UI (invitation inbox). We're
            // ready to accept the selected invitation:
            handleInvitationInboxResult(resultCode, intent);

        } else if (requestCode == RC_WAITING_ROOM) {
            // we got the result from the "waiting room" UI.
            if (resultCode == Activity.RESULT_OK) {
                // ready to start playing
                Log.d(TAG, "Starting game (waiting room returned OK).");
                gameReady = true;
                initializeGame(true);
            } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                // player indicated that they want to leave the room
                leaveRoom();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Dialog was cancelled (user pressed back key, for instance). In our game,
                // this means leaving the room too. In more elaborate games, this could mean
                // something else (like minimizing the waiting room UI).
                leaveRoom();
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        Log.d(TAG, "Select players UI succeeded.");

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        Log.d(TAG, "Invitee count: " + invitees.size());

        // get the automatch criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
        }

        // create the room
        Log.d(TAG, "Creating room...");
        switchToScreen(R.id.screen_wait);
        keepScreenOn();
        resetGameVars();

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .addPlayersToInvite(invitees)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria).build();
        mRealTimeMultiplayerClient.create(mRoomConfig);
        Log.d(TAG, "Room created, waiting for it to be ready...");
    }

    // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.
    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        Log.d(TAG, "Invitation inbox UI succeeded.");
        Invitation invitation = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

        // accept invitation
        if (invitation != null) {
            acceptInviteToRoom(invitation.getInvitationId());
        }
    }

    // Accept the given invitation.
    void acceptInviteToRoom(String invitationId) {
        // accept the invitation
        Log.d(TAG, "Accepting invitation: " + invitationId);

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setInvitationIdToAccept(invitationId)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .build();

        switchToScreen(R.id.screen_wait);
        keepScreenOn();
        resetGameVars();

        mRealTimeMultiplayerClient.join(mRoomConfig)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Room Joined Successfully!");
                    }
                });
    }

    // Activity is going to the background. We have to leave the current room.
    @Override
    public void onStop() {
        Log.d(TAG, "**** got onStop");

        // if we're in a room, leave it.
        leaveRoom();

        // stop trying to keep the screen on
        stopKeepingScreenOn();

        switchToMainScreen();

        super.onStop();
        if (media != null) {
            media.release();
            media = null;
        }
    }

    // Handle back key to make sure we cleanly leave a game if we are in the middle of one
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mCurScreen == R.id.screen_game) {
            leaveRoom();
            return true;
        }
        return super.onKeyDown(keyCode, e);
    }

    /*
     * COMMUNICATIONS SECTION. Methods that implement the game's network
     * protocol.
     */

    // Leave the room.
    void leaveRoom() {
        Log.d(TAG, "Leaving room.");
        mSecondsLeft = 0;
        stopKeepingScreenOn();
        if (mRoomId != null) {
            mRealTimeMultiplayerClient.leave(mRoomConfig, mRoomId)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mRoomId = null;
                            mRoomConfig = null;
                        }
                    });
            switchToScreen(R.id.screen_wait);
        } else {
            switchToMainScreen();
        }
    }

    // Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.
    void showWaitingRoom(Room room) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        mRealTimeMultiplayerClient.getWaitingRoomIntent(room, MIN_PLAYERS)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        // show waiting room UI
                        startActivityForResult(intent, RC_WAITING_ROOM);
                    }
                })
                .addOnFailureListener(createFailureListener("There was a problem getting the waiting room!"));
    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "onConnected(): connected to Google APIs");
        if (mSignedInAccount != googleSignInAccount) {

            mSignedInAccount = googleSignInAccount;

            // update the clients
            mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(this, googleSignInAccount);
            mInvitationsClient = Games.getInvitationsClient(Game.this, googleSignInAccount);

            // get the playerId from the PlayersClient
            PlayersClient playersClient = Games.getPlayersClient(this, googleSignInAccount);
            playersClient.getCurrentPlayer()
                    .addOnSuccessListener(new OnSuccessListener<com.google.android.gms.games.Player>() {
                        @Override
                        public void onSuccess(com.google.android.gms.games.Player player) {
                            mPlayerId = player.getPlayerId();

                            switchToMainScreen();
                        }
                    })
                    .addOnFailureListener(createFailureListener("There was a problem getting the player id!"));
        }

        // register listener so we are notified if we receive an invitation to play
        // while we are in the game
        mInvitationsClient.registerInvitationCallback(mInvitationCallback);

        // get the invitation from the connection hint
        // Retrieve the TurnBasedMatch from the connectionHint
        GamesClient gamesClient = Games.getGamesClient(Game.this, googleSignInAccount);
        gamesClient.getActivationHint()
                .addOnSuccessListener(new OnSuccessListener<Bundle>() {
                    @Override
                    public void onSuccess(Bundle hint) {
                        if (hint != null) {
                            Invitation invitation =
                                    hint.getParcelable(Multiplayer.EXTRA_INVITATION);

                            if (invitation != null && invitation.getInvitationId() != null) {
                                // retrieve and cache the invitation ID
                                Log.d(TAG, "onConnected: connection hint has a room invite!");
                                acceptInviteToRoom(invitation.getInvitationId());
                            }
                        }
                    }
                })
                .addOnFailureListener(createFailureListener("There was a problem getting the activation hint!"));
    }

    private OnFailureListener createFailureListener(final String string) {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                handleException(e, string);
            }
        };
    }

    public void onDisconnected() {
        Log.d(TAG, "onDisconnected()");

        mRealTimeMultiplayerClient = null;
        mInvitationsClient = null;

        switchToMainScreen();
    }

    // Show error message about game being cancelled and return to main screen.
    void showGameError() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.game_problem))
                .setNeutralButton(android.R.string.ok, null).create();

        switchToMainScreen();
    }


    /*
     * UI SECTION. Methods that implement the game's UI.
     */

    void updateRoom(Room room) {
        if (room != null) {
            mParticipants = room.getParticipants();
        }
        if (mParticipants != null) {
//            updatePeerScoresDisplay();
        }
    }

    // Reset game variables in preparation for a new game.
    void resetGameVars() {
        mScore = 0;
        mIsGameFinished = false;
        mParticipantLives.clear();
        mParticipantEnemy.clear();
        mFinishedParticipants.clear();
    }

    private void broadcastGameStart() {
        // Send start signal to all
        mMsgBuf[0] = (byte) 'S';
        for (int i = 0; i < mParticipants.size(); i++) {
            Participant participant = mParticipants.get(i);
            if (i == mParticipantIndex) {
                continue;
            }
            // final score notification must be sent via reliable message
            mRealTimeMultiplayerClient.sendReliableMessage(Arrays.copyOfRange(mMsgBuf, 0, 1),
                    mRoomId, participant.getParticipantId(), new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                        @Override
                        public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {
                            Log.d("COMM", "START message sent");
//                            Log.d("COMM", "  statusCode: " + statusCode);
//                            Log.d("COMM", "  tokenId: " + tokenId);
                            Log.d("COMM", "  recipientParticipantId: " + recipientParticipantId);
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<Integer>() {
                        @Override
                        public void onSuccess(Integer tokenId) {
                            Log.d("COMM", "START message with tokenId: " + tokenId);
                        }
                    });
        }
    }

    void initializeGame(boolean multiplayer) {
        Log.i("ZSX", "Game initialization from " + mParticipants.get(mParticipantIndex).getParticipantId());

        mMultiplayer = multiplayer;
        if (!mMultiplayer) {
            startGame(mMultiplayer);
        } else {
            Collections.sort(mParticipants, new Comparator<Participant>() {
                @Override
                public int compare(Participant p1, Participant p2) {
                    return p1.getParticipantId().compareTo(p2.getParticipantId());
                }
            });
            for (int i = 0; i < mParticipants.size(); i++) {
                Participant participant = mParticipants.get(i);
                mParticipantLives.put(participant.getParticipantId(), 3);
                if (participant.getParticipantId().equals(mMyId)) {
                    mParticipantIndex = i;
                }
            }

            if (mParticipantIndex == 0) {
                Log.v("COMM", "Broadcasting colors from " + mParticipants.get(mParticipantIndex).getParticipantId());
                broadcastPlayersColor();
                broadcastGameStart();
                startGame(mMultiplayer);
            }
        }
    }

    void restartGame() {
        Log.i("COMM", "Restarting game call from " + mParticipants.get(mParticipantIndex).getParticipantId());
        if (!mMultiplayer) {
            startGame(mMultiplayer);

        } else {
            for (int i = 0; i < mParticipants.size(); i++) {
                Participant participant = mParticipants.get(i);
                if (i != mParticipantIndex) {
                    Enemy enemy = mParticipantEnemy.get(participant.getParticipantId());
                    enemy.setEnemyPosition(getInitialPosition(i));
                    mParticipantEnemy.put(participant.getParticipantId(), enemy);
                }
            }
            mFinishedParticipants.clear();
            isRestarting = true;
            if (mParticipantIndex == 0) {
                Log.v("COMM", "Broadcasting restart game from " + mParticipants.get(mParticipantIndex).getParticipantId());

                broadcastGameStart();
                startGame(mMultiplayer);
            } else {
                Log.v("COMM", "Waiting for game restart (" + mParticipants.get(mParticipantIndex).getParticipantId() + ")");
            }
        }
    }

    // Start the gameplay phase of the game.
    void startGame(boolean multiplayer) {

        mMultiplayer = multiplayer;
//        Intent intent = new Intent(this, Game.class);
//        startActivity(intent);

        //turn title off
//        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //set to full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ConstraintLayout gameScreen = findViewById(R.id.screen_game);
        SurfaceView viewTemplate = findViewById(R.id.game_panel);
        if (mGamePanelParams == null) {
            mGamePanelParams = viewTemplate.getLayoutParams();
        }
        gameScreen.removeView(viewTemplate);

        backgroundImage.setBackgroundColor(playerColor);
        mStartGameCountdown = findViewById(R.id.start_countdown);
        ViewGroup.LayoutParams textViewParam = mStartGameCountdown.getLayoutParams();
        gameScreen.removeView(mStartGameCountdown);
        Int2 playerPosition = new Int2(GRID_SIZE.x / 2, GRID_SIZE.y / 2);
        if (multiplayer) {
            if (mGamePanel != null) {
                mGamePanel.resetTrailsAndEnemies();
            }
            broadcastPlayerInfo();
            playerPosition = getInitialPosition(mParticipantIndex);
            int playerLives = mParticipantLives.get(mParticipants.get(mParticipantIndex).getParticipantId());
            for (int i = 0; i < 3; i++) {
                for (Participant participant : mParticipants) {
                    if (participant.getParticipantId().equals(mParticipants.get(mParticipantIndex).getParticipantId())) {
                        if (i + 1 <= playerLives)
                            mPLayerLives.get(i).setVisibility(View.VISIBLE);
                        else
                            mPLayerLives.get(i).setVisibility(View.GONE);
                    } else {
                        if (i + 1 <= mParticipantLives.get(participant.getParticipantId()))
                            mEnemyLives.get(i).setVisibility(View.VISIBLE);
                        else
                            mEnemyLives.get(i).setVisibility(View.GONE);
                    }
                }
            }
        }
        gameScreen.removeView(mWonOverlay);
        gameScreen.removeView(mLostOverlay);
        mGamePanel = new GamePanel(this, GRID_SIZE, playerPosition);
        gameScreen.addView(mGamePanel, mGamePanelParams);
        gameScreen.addView(mStartGameCountdown, textViewParam);
        gameScreen.addView(mWonOverlay);
        gameScreen.addView(mLostOverlay);
        mLostOverlay.setVisibility(View.GONE);
        mWonOverlay.setVisibility(View.GONE);
        switchToScreen(R.id.screen_game);

        mStartGameCountdown.setVisibility(View.VISIBLE);
        new CountDownTimer(2000, 50) {
            public void onTick(long millisUntilFinished) {
                BigDecimal decimal = round((millisUntilFinished / 1000.0f), 2);
                mStartGameCountdown.setText(getString(R.string.start_countdown_label, String.valueOf(decimal)));
            }

            public void onFinish() {
                isRestarting = false;
                mStartGameCountdown.setVisibility(View.GONE);
                mGamePanel.startGameUpdate();
            }
        }.start();
    }

    private void broadcastPlayersColor() {
        if (!mMultiplayer) {
            // playing single-player mode

            Log.i("COMM", "No color broadcast in single player");
            return;
        }
        // Host color
        Random rand = new Random();
        String[] colors = {"#76FF03", "#FF3D00", "#18FFFF", "#FFFF00", "#E040FB", "#FF5252"};
        int colorIndex = rand.nextInt(colors.length);
        int color = Color.parseColor(colors[colorIndex]);

        playerColor = color;

        mMsgBuf[0] = (byte) 'I';

        int nbParticipant = mParticipants.size();
        byte[] colorByteArray;
        for (int i = 0; i < nbParticipant; i++) {
            Participant participant = mParticipants.get(i);
            if (i == mParticipantIndex) {
                // Fill buffer with host color for client's message
                colorByteArray = intToByteArray(playerColor);
                mMsgBuf[4 * i + 1] = colorByteArray[0];
                mMsgBuf[4 * i + 2] = colorByteArray[1];
                mMsgBuf[4 * i + 3] = colorByteArray[2];
                mMsgBuf[4 * i + 4] = colorByteArray[3];
            } else {
                // Different color for all players
                while (color == playerColor) {
                    colorIndex = rand.nextInt(colors.length);
                    color = Color.parseColor(colors[colorIndex]);
                }
                // Create Enemy for host
                mParticipantEnemy.put(participant.getParticipantId(), new Enemy(color, getInitialPosition(i), GRID_SIZE));

                // Fill buffer with player's color for client's message
                colorByteArray = intToByteArray(color);
                mMsgBuf[4 * i + 1] = colorByteArray[0];
                mMsgBuf[4 * i + 2] = colorByteArray[1];
                mMsgBuf[4 * i + 3] = colorByteArray[2];
                mMsgBuf[4 * i + 4] = colorByteArray[3];
            }
        }
        // Send the set of colors to everyone
        for (int i = 0; i < nbParticipant; i++) {
            Participant participant = mParticipants.get(i);
            if (i == mParticipantIndex) {
                continue;
            }
            // final score notification must be sent via reliable message
            mRealTimeMultiplayerClient.sendReliableMessage(Arrays.copyOfRange(mMsgBuf, 0, nbParticipant * 4 + 2),
                    mRoomId, participant.getParticipantId(), new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                        @Override
                        public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {
//                            Log.d(TAG, "  statusCode: " + statusCode);
//                            Log.d(TAG, "  tokenId: " + tokenId);
                            Log.d("COMM", "Sending COLOR message to " + recipientParticipantId);
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<Integer>() {
                        @Override
                        public void onSuccess(Integer tokenId) {
                            Log.d("COMM", "COLOR message sent");
                        }
                    });
        }
        // Start wifi lost timeout

        if (mWifiTimeout == null) {
            mWifiTimeout = new CountDownTimer(6000, 50) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    Log.v("ZSX", "WIFI timeout");
                    Toast.makeText(getApplicationContext(), "Connection to opponent was lost", Toast.LENGTH_SHORT).show();
                    resetGameVars();
                    leaveRoom();
                }
            }.start();
        }
    }

    public void broadcastPlayerInfo() {
        if (mProfile != null) {
            broadCastPlayerProfile();
        } else {
            Bitmap defaultPicture = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            mProfile = new Profile(this.mSignedInAccount.getDisplayName(), this.currentLocation, PictureTypeConverter.toString(defaultPicture));
            this.tronViewModel.saveProfile(this.mProfile);
            broadCastPlayerProfile();
        }
    }

//    public static  byte[] charToByteArray(char aChar){
//        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putChar(aChar).array();
//    }

//    public static int byteArrayToChar(byte [] byteBarray){
//        return ByteBuffer.wrap(byteBarray).order(ByteOrder.LITTLE_ENDIAN).getChar();
//    }

    private void broadCastPlayerProfile() {
        tronViewModel.getLocationLiveData().removeObserver(locationObserver);
        SerializableProfile serializableProfile = new SerializableProfile(mProfile);
        byte[] bytes = SerializationUtils.serialize(serializableProfile);
        byte[] msg = new byte[bytes.length + 1];
        msg[0] = 'M';
        System.arraycopy(bytes, 0, msg, 1, bytes.length);

        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(mMyId))
                continue;

            if (p.getStatus() != Participant.STATUS_JOINED)
                continue;

            mRealTimeMultiplayerClient.sendReliableMessage(msg,
                    mRoomId, p.getParticipantId(), (statusCode, tokenId, recipientParticipantId) -> {
                        Log.d("COMM", "Profile info message sent");
                        Log.d("COMM", "  recipientParticipantId: " + recipientParticipantId);
                    })
                    .addOnSuccessListener(tokenId -> Log.d("COMM", "Profile info message with tokenId: " + tokenId));
        }


    }

    public void broadcastCollision() {
        // Already registered collision(s)/Restarting
        if (isRestarting || mParticipants == null || mFinishedParticipants.contains(mPlayerId)) {
            return;
        }
        msgCollBuf[0] = (byte) 'C';
        isWaitingCollisionAck = true;
        mFinishedParticipants.add(mParticipants.get(mParticipantIndex).getParticipantId());
        mParticipantLives.put(mParticipants.get(mParticipantIndex).getParticipantId(), mParticipantLives.get(mParticipants.get(mParticipantIndex).getParticipantId()) - 1);
        // Send to every other participant.
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(mMyId)) {
                continue;
            }
            if (p.getStatus() != Participant.STATUS_JOINED) {
                continue;
            }
            // collision notification must be sent via reliable message to restart game
            mRealTimeMultiplayerClient.sendReliableMessage(msgCollBuf,
                    mRoomId, p.getParticipantId(), new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                        @Override
                        public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {
//                            Log.d(TAG, "  statusCode: " + statusCode);
//                            Log.d(TAG, "  tokenId: " + tokenId);
                            Log.d("COMM", "Sending COLLISION message to " + recipientParticipantId);
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<Integer>() {
                        @Override
                        public void onSuccess(Integer tokenId) {
                            Log.d("COMM", "COLLISION message sent");
                        }
                    });
        }
    }

    public void sendCollisionAck(String participantId) {
        if (isRestarting) {
            return;
        }
        mMsgBuf[0] = (byte) 'A';

        // Send to every other participant.
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(mMyId)) {
                continue;
            }
            if (p.getStatus() != Participant.STATUS_JOINED) {
                continue;
            }
            // collision notification must be sent via reliable message to restart game
            mRealTimeMultiplayerClient.sendReliableMessage(mMsgBuf,
                    mRoomId, participantId, new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                        @Override
                        public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {
//                            Log.d(TAG, "  statusCode: " + statusCode);
//                            Log.d(TAG, "  tokenId: " + tokenId);
                            Log.d("COMM", "Sending ACK COLLISION message to " + recipientParticipantId);
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<Integer>() {
                        @Override
                        public void onSuccess(Integer tokenId) {
                            Log.d("COMM", "ACK COLLISION message sent");
                            if (!isWaitingCollisionAck) {
                                mFinishedParticipants.add(mParticipants.get(mParticipantIndex).getParticipantId());
                                checkCollisionResolve();
                            }
                        }
                    });
        }
    }

    public void broadcastPosition(Player player) {
        if (!mMultiplayer) {
            // playing single-player mode
            return;
        }

        // First byte in message indicates position update
        mMsgBuf[0] = (byte) 'P';
        // Then we send int pos as 4 bytes
        byte[] playerPosX = intToByteArray(player.getPlayerPosition().x);
        mMsgBuf[1] = playerPosX[0];
        mMsgBuf[2] = playerPosX[1];
        mMsgBuf[3] = playerPosX[2];
        mMsgBuf[4] = playerPosX[3];
        byte[] playerPosY = intToByteArray(player.getPlayerPosition().y);
        mMsgBuf[5] = playerPosY[0];
        mMsgBuf[6] = playerPosY[1];
        mMsgBuf[7] = playerPosY[2];
        mMsgBuf[8] = playerPosY[3];

        // Send to every other participant.
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(mMyId)) {
                continue;
            }
            if (p.getStatus() != Participant.STATUS_JOINED) {
                continue;
            }
            // it's an interim score notification, so we can use unreliable
            mRealTimeMultiplayerClient.sendUnreliableMessage(Arrays.copyOfRange(mMsgBuf, 0, 9), mRoomId,
                    p.getParticipantId());
        }
    }

    public void broadcastTurnReliable(Player player) {
        if (!mMultiplayer) {
            // playing single-player mode
            return;
        }

        // First byte in message indicates position update
        mMsgBuf[0] = (byte) 'P';
        // Then we send int pos as 4 bytes
        byte[] playerPosX = intToByteArray(player.getPlayerPosition().x);
        mMsgBuf[1] = playerPosX[0];
        mMsgBuf[2] = playerPosX[1];
        mMsgBuf[3] = playerPosX[2];
        mMsgBuf[4] = playerPosX[3];
        byte[] playerPosY = intToByteArray(player.getPlayerPosition().y);
        mMsgBuf[5] = playerPosY[0];
        mMsgBuf[6] = playerPosY[1];
        mMsgBuf[7] = playerPosY[2];
        mMsgBuf[8] = playerPosY[3];

        // Send to every other participant.
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(mMyId)) {
                continue;
            }
            if (p.getStatus() != Participant.STATUS_JOINED) {
                continue;
            }
            // it's an interim score notification, so we can use unreliable
            mRealTimeMultiplayerClient.sendReliableMessage(Arrays.copyOfRange(mMsgBuf, 0, 9),
                    mRoomId, p.getParticipantId(), new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                        @Override
                        public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {
                            Log.d("COMM", "POSITION RELIABLE message sent");
//                            Log.d(TAG, "  statusCode: " + statusCode);
//                            Log.d(TAG, "  tokenId: " + tokenId);
                            Log.d("COMM", "  recipientParticipantId: " + recipientParticipantId);
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<Integer>() {
                        @Override
                        public void onSuccess(Integer tokenId) {
                            Log.d("COMM", "POSITION RELIABLE message with tokenId: " + tokenId);
                        }
                    });
        }
    }

    // Broadcast my score to everybody else.
    void broadcastScore(boolean finalScore) {
        if (!mMultiplayer) {
            // playing single-player mode
            return;
        }

        // First byte in message indicates whether it's a final score or not
        mMsgBuf[0] = (byte) (finalScore ? 'F' : 'U');

        // Second byte is the score.
        mMsgBuf[1] = (byte) mScore;

        // Send to every other participant.
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(mMyId)) {
                continue;
            }
            if (p.getStatus() != Participant.STATUS_JOINED) {
                continue;
            }
            if (finalScore) {
                // final score notification must be sent via reliable message
                mRealTimeMultiplayerClient.sendReliableMessage(mMsgBuf,
                        mRoomId, p.getParticipantId(), new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                            @Override
                            public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {
                                Log.d("COMM", "Score message sent");
                                Log.d("COMM", "  statusCode: " + statusCode);
                                Log.d("COMM", "  tokenId: " + tokenId);
                                Log.d("COMM", "  recipientParticipantId: " + recipientParticipantId);
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<Integer>() {
                            @Override
                            public void onSuccess(Integer tokenId) {
                                Log.d("COMM", "Score message with tokenId: " + tokenId);
                            }
                        });
            } else {
                // it's an interim score notification, so we can use unreliable
                mRealTimeMultiplayerClient.sendUnreliableMessage(mMsgBuf, mRoomId,
                        p.getParticipantId());
            }
        }
    }

    void switchToScreen(int screenId) {
        // make the requested screen visible; hide all others.
        for (int id : SCREENS) {
            findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
        }
        mCurScreen = screenId;

        if (mCurScreen != SCREENS[0]) {
            if (mWifiTimeout != null) {
                mWifiTimeout.cancel();
                mWifiTimeout = null;
            }
            ConstraintLayout gameScreen = findViewById(R.id.screen_game);
            SurfaceView viewTemplate = findViewById(R.id.game_panel);
            if (mGamePanelParams == null) {
                mGamePanelParams = viewTemplate.getLayoutParams();
            }
            gameScreen.removeView(viewTemplate);
        }

        // should we show the invitation popup?
        boolean showInvPopup;
        if (mIncomingInvitationId == null) {
            // no invitation, so no popup
            showInvPopup = false;
        } else if (mMultiplayer) {
            // if in multiplayer, only show invitation on main screen
            showInvPopup = (mCurScreen == R.id.screen_main);
        } else {
            // single-player: show on main screen and gameplay screen
            showInvPopup = (mCurScreen == R.id.screen_main || mCurScreen == R.id.screen_game);
        }
        findViewById(R.id.invitation_popup).setVisibility(showInvPopup ? View.VISIBLE : View.GONE);
    }

    // updates the label that shows my score
//    void updateScoreDisplay() {
//        ((TextView) findViewById(R.id.my_score)).setText(formatScore(mScore));
//    }

    void switchToMainScreen() {
        if (mRealTimeMultiplayerClient != null) {
            mSurfaceView.setVisibility(View.VISIBLE);
            switchToScreen(R.id.screen_main);
        } else {
            switchToScreen(R.id.screen_sign_in);
        }
    }

    // formats a time as a X.XX number
//    String formatTime(float i) {
//        String s = String.valueOf(i);
//        return i < 1 ? "0" + s : s.length() == 2 ? "0" + s : s;
//    }

    // updates the screen with the scores from our peers
//    void updatePeerScoresDisplay() {
//        ((TextView) findViewById(R.id.score0)).setText(
//                getString(R.string.score_label, formatScore(mScore)));
//        int[] arr = {
//                R.id.score1, R.id.score2, R.id.score3
//        };
//        int i = 0;
//
//        if (mRoomId != null) {
//            for (Participant p : mParticipants) {
//                String pid = p.getParticipantId();
//                if (pid.equals(mMyId)) {
//                    continue;
//                }
//                if (p.getStatus() != Participant.STATUS_JOINED) {
//                    continue;
//                }
//                int score = mParticipantLives.containsKey(pid) ? mParticipantLives.get(pid) : 0;
//                ((TextView) findViewById(arr[i])).setText(formatScore(score) + " - " +
//                        p.getDisplayName());
//                ++i;
//            }
//        }
//
//        for (; i < arr.length; ++i) {
//            ((TextView) findViewById(arr[i])).setText("");
//        }
//    }

    /*
     * MISC SECTION. Miscellaneous methods.
     */

    // formats a score as a three-digit number
    String formatScore(int i) {
        if (i < 0) {
            i = 0;
        }
        String s = String.valueOf(i);
        return s.length() == 1 ? "00" + s : s.length() == 2 ? "0" + s : s;
    }

    // Sets the flag to keep this screen on. It's recommended to do that during
    // the
    // handshake when setting up a game, because if the screen turns off, the
    // game will be
    // cancelled.
    void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void leaveGameScreen(View view) {
        if (mIsGameFinished) {
            resetGameVars();
            leaveRoom();
        }
    }

    private class DelayedAck implements Runnable {
        String sender;

        public DelayedAck(String sender) {
            this.sender = sender;
        }

        @Override
        public void run() {
            sendCollisionAck(sender);
        }
    }
}