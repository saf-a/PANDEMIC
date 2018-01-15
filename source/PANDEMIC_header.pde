//都道府県
ArrayList<prefecture> todoufuken = new ArrayList<prefecture>();
//プレイヤー
ArrayList<player> players = new ArrayList<player>();
//0:ボード 1:感染カード 2:プレイヤーカード 3:調査基地
ArrayList<ImageSet> boardImgSet = new ArrayList<ImageSet>();
//0:チャーター便による移動　1:調査基地の設置 2:治療薬の発見 3:知識の共有 4:イベントカード
ArrayList<ImageSet> playerActionImgSet = new ArrayList<ImageSet>();
//治療薬 0:青 1:赤 2:青緑 3:紫
ArrayList<ImageSet> TreatDiseases = new ArrayList<ImageSet>();
ArrayList<ImageSet> cureMarkers = new ArrayList<ImageSet>();
//数字
ArrayList<ImageSet> numbers = new ArrayList<ImageSet>();
//上下
ArrayList<ImageSet> jouge = new ArrayList<ImageSet>();

final color RED      = color( 255, 0  , 0   );
final color BLUE     = color( 100, 100, 230 );
final color GREEN    = color( 0  , 255, 0   );
final color DARKBLUE = color( 0  , 0  , 139 );
final color PURPLE   = color( 150, 0  , 150 );
final color YELLOW   = color( 255, 255, 100 );
final color LIGHTSEAGREEN = color( 32 , 178, 170 );
final color BLACK    = color( 0  , 0  , 0   );
final color WHITE    = color( 255, 255, 255 );
final color ENJI     = color( 179, 66 , 74  );
final color cGRAY    = color( 190, 190, 190 );
final color[] pathogenColorPattern = {BLUE, ENJI, LIGHTSEAGREEN, PURPLE, cGRAY};
final color[] textColorPattern = {BLACK, cGRAY, WHITE};

PVector bs;                    //位置調整用
Draws draws;                   //背景等の描写用
PHASE phase;                   //フェーズ管理
Phases phases;                 //フェーズの中身管理
GAME_STATUS gameStatus;        //ゲームステータス用

String[] rule;                 //ヘルプ用

float TEXT_SIZE;               //テキストのサイズ
float ELLIPSE_SIZE;            //ellipseのサイズ
float RECT_SIZE;               //rectのサイズ
int ACTION_COUNT = 4;          //アクションのカウント

final int CURE = -999;         //治療薬用

final int todoufukenNum = 47;  //都道府県の数
final int eventCardNum = 5;    //イベントカードの数

final int AIRLIFT = 47;              //空輸
final int FORECAST = 48;             //予測
final int GOVERNMENT_GRANT = 49;     //政府の補助
final int ONE_QUIET_NIGHT = 50;      //静かな夜
final int RESILIENT_POPULATION = 51; //人口回復

enum GAME_OVER{
  LOSE,      //敗北
  CONTINUE,  //続行
  WIN;       //勝利
}

enum PHASE{
  GAME_DIFFICULTY_INPUT,     //ゲーム難易度入力
  PLAYER_NUMBER_INPUT,       //人数入力
  PLAYER_NAME_INPUT,         //名前入力
  GAME,                      //ゲーム
  HAND_LIMIT,                //手札を捨てる
  CHARTER_FLIGHT,            //チャーター便による移動
  DISCOVER_A_CURE,           //治療薬の発見
  SHARE_KNOWLEDGE,           //知識の共有
  BUILD_A_RESEARCH_STATION,  //作戦エキスパートによる調査基地の設置
  SPECIAL_SKILL,             //特殊技能
  EVENT_CARD,                //イベントカード
  GAME_OVER;                 //ゲーム終了
}

enum TURN{
  PLAYER1(0),   //プレイヤー1
  PLAYER2(1),   //プレイヤー2
  PLAYER3(2),   //プレイヤー3
  PLAYER4(3);   //プレイヤー4
  final int n;
  private TURN( int n ){
    this.n = n;
  }
}
TURN MAIN_TURN = TURN.PLAYER1;

enum ROLES{                       //プレイヤーの役職
  CONTINGENCY_PLANNER(true,0,255,255),  //危機管理官
  DISPATCHER(true,255,0,255),           //通信指令員
  OPERATIONS_EXPERT(true,170,204,59),   //作戦エキスパート
  MEDIC(false,230,121,40),              //衛生兵
  SCIENTIST(false,210,209,192),         //科学者
  RESEARCHER(true,153,76,0),            //研究員
  QUARANTINE_SPECIALIST(false,0,128,0); //検疫官
  
  final int[] roleColor = new int[3];
  final boolean playerSkillFlag;
  private ROLES( boolean playerSkillFlag, int... roleColor ){
    this.playerSkillFlag = playerSkillFlag;
    arrayCopy( roleColor, this.roleColor );
  }
}

StringList playerNames = new StringList();  //プレイヤーの名前
StringList charLst = new StringList();      //プレイヤーの名前入力用リスト
int epidemicCardNumber = 0;                 //エピデミックカードの数
int playerNumber = 0;                       //プレイヤーの人数