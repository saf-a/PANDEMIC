import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class PANDEMIC extends PApplet {

public void setup(){
  
  
  //size(displayWidth,displayHeight);
  //size(1200,1000);
  frameRate( 60 );
  
  textFont( createFont( "MS Gothic",48,true ) ); //\u30d5\u30a9\u30f3\u30c8\u6307\u5b9a
  
  draws = new Draws();
  phases = new Phases();
  gameStatus = new GAME_STATUS();  
    
  //\u5909\u6570\u306e\u30bb\u30c3\u30c8\u30a2\u30c3\u30d7
  setupVar();
  
  textAlign( CENTER, CENTER );
  textSize( TEXT_SIZE );
  ellipseMode( CENTER );
  
  strokeWeight(1);
}

public void draw(){
  drawBackground();
  
  //\u30a2\u30af\u30b7\u30e7\u30f34\u56de\u3067\u30bf\u30fc\u30f3\u30c1\u30a7\u30f3\u30b8
  if( ACTION_COUNT == 0 && phase != PHASE.HAND_LIMIT ){
    turnChange();
  }
  
  //\u30d8\u30eb\u30d7\u306e\u8868\u793a
  if( phase == PHASE.GAME ){
    draws.helpDraw();
  }
  gameStatus.update();
  //line( mouseX, 0, mouseX, height );
  //line( 0, mouseY, width, mouseY );
}

public void keyPressed(){
  switch( phase ){
    case GAME_DIFFICULTY_INPUT:
    case PLAYER_NUMBER_INPUT:
    case PLAYER_NAME_INPUT:
      phases.gameInputPhase();
    break;
    case GAME_OVER:
      gameStatus.gameOverSettingReset();
      setupVar();
      phase = PHASE.GAME_DIFFICULTY_INPUT;
    break;
    default:
  }
  
}

public void mouseReleased(){
  gameStatus.pressFlag = true;
}

public void mousePressed(){
  //println( "wall.add( new PVector( width / " + nfc(float(width) / float(mouseX),3) + ", height / " + nfc(float(height) / float(mouseY),3) + " ) );" );
  
  switch( mouseButton ){
    case LEFT:
      if( !gameStatus.helpFlag ){
        switch( phase ){
          case GAME:
            phases.gameMainPhase();
          break;
          default:
        }
      }else{
        gameStatus.helpFlag = !gameStatus.helpFlag;
      }
    break;
    case RIGHT:
      if( phase != PHASE.HAND_LIMIT ){
        gameStatus.resetSetting();
      }
    break;
    default:
  }
}

public void drawBackground(){
  switch( phase ){
    case GAME_DIFFICULTY_INPUT:
    case PLAYER_NUMBER_INPUT:
    case PLAYER_NAME_INPUT:
      draws.gameInputDraw();
    break;
    case GAME_OVER:
      draws.gameOverDraw();
    break;
    case GAME:
      draws.gameMainDraw();
    break;
    case HAND_LIMIT:
      draws.gameMainDraw();
      handLimitProcess();
    break;
    case DISCOVER_A_CURE:
      draws.gameMainDraw();
      discoverCureProcess();
    break;
    case SHARE_KNOWLEDGE:
      draws.gameMainDraw();
      shareKnowledgeProcess();
    break;
    case EVENT_CARD:
      draws.gameMainDraw();
      eventCardProcess();
    break;
    case SPECIAL_SKILL:
      draws.gameMainDraw();
      specialSkillProcess();
    break;
    default:
  }
}
class prefecture{
  private String name;                      //\u90fd\u9053\u5e9c\u770c\u540d
  private int position;                     //\u30dd\u30b8\u30b7\u30e7\u30f3
  private float x;                          //x\u5ea7\u6a19
  private float y;                          //y\u5ea7\u6a19
  private ArrayList<PVector> wall = new ArrayList<PVector>();  //\u30c7\u30a3\u30b9\u30d7\u30ec\u30a4\u7528\u58c1
  private int col;                          //\u75c5\u539f\u4f53\u8272
  private int[] pathogenCnt = new int[4];   //\u75c5\u539f\u4f53\u30ab\u30a6\u30f3\u30c8 0\u9752 1\u8d64 2\u9752\u7dd1 3\u7d2b
  private IntList adjacent = new IntList(); //\u96a3\u63a5\u3059\u308b\u90fd\u9053\u5e9c\u770c\u60c5\u5831
  private boolean researchStation;          //\u62e0\u70b9\u6709\u7121
  
  prefecture( String name, int position, float x, float y, ArrayList<PVector> wall, int col, int pathogenCnt, IntList adjacent, boolean researchStation ){
    this.name = name;
    this.position = position;
    this.x = x;
    this.y = y;
    this.wall = wall;
    this.col = col;
    this.pathogenCnt[0] = pathogenCnt;  //\u9752
    this.pathogenCnt[1] = pathogenCnt;  //\u8d64
    this.pathogenCnt[2] = pathogenCnt;  //\u9752\u7dd1
    this.pathogenCnt[3] = pathogenCnt;  //\u7d2b
    this.adjacent = adjacent;
    this.researchStation = researchStation;
  }
  
  public int display( boolean moveFlag ){
    int i, ret = -1;
    
    //\u79fb\u52d5\u53ef\u80fd\u306f\u8272\u3092\u8584\u304f\u3059\u308b
    if( moveFlag ){
      fill( pathogenColorPattern[col], 100 );
    }else{
      fill( pathogenColorPattern[col] ); 
    }
    
    //\u9078\u629e\u4e2d\u306e\u30de\u30b9\u306e\u8272\u3068\u4f4d\u7f6e\u60c5\u5831
    if( isHit() ){
      fill( WHITE );
      ret = position;
    }
    
    //\u30de\u30b9\u306e\u8868\u793a
    stroke( BLACK );
    beginShape();
    for( i = 0; i < wall.size(); i++ ){
      vertex( wall.get( i ).x, wall.get( i ).y );
    }
    endShape();
    
    //\u8abf\u67fb\u57fa\u5730\u304c\u3042\u308c\u3070\u8868\u793a
    if( researchStation == true ){
      boardImgSet.get(3).display( x + TEXT_SIZE * 0.5f, y - TEXT_SIZE * 0.5f, ELLIPSE_SIZE * 0.5f, ELLIPSE_SIZE * 0.5f );
    }
    
    //\u30de\u30b9\u306b\u540d\u524d\u3092\u5165\u308c\u308b
    fill( BLACK );
    textSize( TEXT_SIZE );
    text( name, x, y );
    
    return ret;
  }
  
  public void pathogenDisplay(){
    int i, j;
    float pathogenSize = ( width + height ) / 300, offset = 0;
    
    //\u30de\u30b9\u306e\u5de6\u306b\u75c5\u539f\u4f53\u30de\u30fc\u30ab\u30fc\u30bb\u30c3\u30c8( 0\u30683\u306f1\u500b\u5206\u53f3\u306b\u79fb\u52d5)
    for( i = 0; i < pathogenCnt.length; i++ ){
      fill( pathogenColorPattern[i] );
      if( i == 0 || i == 3 ){
        offset = pathogenSize;
      }else{
        offset = 0;
      }
      for( j = 0; j < pathogenCnt[i]; j++ ){
        rect( x - ( pathogenSize * 2.5f ) - ( j * pathogenSize ) + offset, y - ( pathogenSize * 2 ) + ( pathogenSize * i ), pathogenSize, pathogenSize );
      }
    }
  }
  
  public boolean setResearchStation(){
    if( researchStation == false ){
      researchStation = true;
      gameStatus.researchStationList.append( position );
      return true;
    }else{
      return false;
    }
  }
  
  public boolean isHit(){
    int hitCnt = 0,st,sp;
    for( int hitLineNo = 0; hitLineNo < gameStatus.hitLine.size(); hitLineNo++ ){  //\u5224\u5b9a\u7528\u7dda
      for( int lineNo = 0; lineNo < wall.size() - 1; lineNo++ ){  //\u7dda
        st = lineNo; sp = lineNo + 1;
        //\u5224\u5b9a\u7dda\u3068\u58c1\u306e\u4ea4\u5dee\u5224\u5b9a
        if( isCollisionSide( wall.get( st ), wall.get( sp ), gameStatus.centerPoint, gameStatus.hitLine.get( hitLineNo ) ) && 
            isCollisionSide( gameStatus.centerPoint, gameStatus.hitLine.get( hitLineNo ), wall.get( st ), wall.get( sp ) ) ){
          hitCnt++;
          break;
        }
      }
    }
    //\u4e0a\u4e0b\u5de6\u53f3\u306e\u5224\u5b9a\u7dda\u3068\u58c1\u304c\u4ea4\u5dee\u3057\u3066\u3044\u305f\u3089
    if( hitCnt == 4 ){
      return true;
    }else{
      return false;
    }
  }
  
  public boolean isCollisionSide( PVector r1, PVector r2, PVector p1, PVector p2 ){
    float   t1, t2;
    
    //\u885d\u7a81\u5224\u5b9a\u8a08\u7b97
    t1 = ( r1.x - r2.x ) * ( p1.y - r1.y ) + ( r1.y - r2.y ) * ( r1.x - p1.x );
    t2 = ( r1.x - r2.x ) * ( p2.y - r1.y ) + ( r1.y - r2.y ) * ( r1.x - p2.x );
    
    //\u305d\u308c\u305e\u308c\u306e\u6b63\u8ca0\u304c\u7570\u306a\u308b\uff08\u7a4d\u304c\u8ca0\u306b\u306a\u308b\uff09\u304b\u30010\uff08\u70b9\u304c\u76f4\u7dda\u4e0a\u306b\u3042\u308b\uff09
    //\u306a\u3089\u30af\u30ed\u30b9\u3057\u3066\u3044\u308b
    if( t1 * t2 < 0 || t1 == 0 || t2 == 0 ){
      return true; //\u30af\u30ed\u30b9\u3057\u3066\u3044\u308b
    }else{
      return false; //\u30af\u30ed\u30b9\u3057\u306a\u3044
    }
  }
}

class player{
  private String name;                    //\u540d\u524d
  private int position;                   //\u30dd\u30b8\u30b7\u30e7\u30f3
  private float x;                        //x
  private float y;                        //y
  private int no;                         //\u30ca\u30f3\u30d0\u30fc
  private ROLES role;                     //\u5f79\u8077
  private boolean playerSkillFlag;        //\u30d7\u30ec\u30a4\u30e4\u30fc\u30b9\u30ad\u30eb\u6709\u7121
  private int col;                      //\u8272
  private IntList cards = new IntList();  //\u624b\u6301\u3061\u30ab\u30fc\u30c9
  
  player( String name,int position, float x, float y, int no, ROLES role ){
    this.name = name;
    this.position = position;
    this.x = x;
    this.y = y;
    this.no = no;
    this.role = role;
    this.playerSkillFlag = role.playerSkillFlag;
    this.col = color( role.roleColor[0], role.roleColor[1], role.roleColor[2] );
  }
  
  public void setCard( int card ){
    cards.append( card );
    if( cards.size() >= 8 ){
      phase = PHASE.HAND_LIMIT;
      draws.targetPlayerNo = no;
      gameStatus.pressFlag = false;
    }
    cards.sort();
  }
  
  public int removeCard( int card ){
    for( int i = 0; i < cards.size(); i++ ){
      if( card == cards.get(i) ){
        gameStatus.playerDiscardPile.append( card );
        return cards.remove( i );
      }
    }
    return -1;
  }
  
  public void display(){
    prefecture t;
    
    //\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u30b3\u30de\u8868\u793a
    float resize = ELLIPSE_SIZE / 3;
    float px = x + resize / 2, py = y + resize / 2;
    if( MAIN_TURN.n == no ){
      fill( WHITE );
    }else{
      fill( col );
    }
    strokeWeight( 1 );
    triangle( px, py, px + resize / 1.5f, py + resize * 1.5f, px - resize / 1.5f, py + resize * 1.5f );
    ellipse ( px, py, resize, resize );
    fill( BLACK );
    
    //\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u624b\u672d\u306e\u8868\u793a
    textAlign( LEFT, CENTER );
    float wid = 1.125f * bs.x;
    if( MAIN_TURN.n == no ){
      stroke( RED );
      strokeWeight( 3 );
    }else{
      noStroke();
    }
    fill( col, 100 );
    rect( 7.875f * bs.x - ( no * wid ), 7 * bs.y, wid, 2 * bs.y );
    fill( BLACK );
    textSize( TEXT_SIZE * 2.5f );
    text( name, 8 * bs.x - ( no * wid ), 7.125f * bs.y );
    textSize( TEXT_SIZE * 1.5f );
    stroke( BLACK );
    strokeWeight( 1 );
    for( int i = 0; i < cards.size(); i++ ){
      t = todoufuken.get( cards.get(i) );
      fill( BLACK );
      text( t.name, 8 * bs.x - ( no * wid ), 7.5f * bs.y + i * ( TEXT_SIZE * 1.5f ) );
      fill( pathogenColorPattern[t.col] );
      rect( 7.875f * bs.x - ( no * wid ), 7.5f * bs.y + i * ( TEXT_SIZE * 1.5f ) - TEXT_SIZE * 0.25f, TEXT_SIZE, TEXT_SIZE );
    }
    textSize( TEXT_SIZE );
    textAlign( CENTER, CENTER );
  }
  
  public void move( int pos ){
    prefecture t = todoufuken.get( pos );
    position = t.position;
    x = t.x;
    y = t.y;
  }
}

class ImageSet{
  private PImage image;
  private float x;
  private float y;
  private float w;
  private float h;

  ImageSet( String name ){
    this.image = loadImage( name, "png" );
  }
  
  ImageSet( String name, float x, float y, float w, float h ){
    this.image = loadImage( name, "png" );
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
  }
  
  public void display(){
    image( image, x, y, w, h ); 
  }
  
  public void display( float x, float y, float w, float h ){
    image( image, x, y, w, h );
  }
  
  public boolean isHit(){
    return ( x < mouseX && mouseX < x + w  && y < mouseY && mouseY < y + h );
  }
}//ImageSet

class GAME_STATUS{
  private boolean pressFlag;                               //\u30a2\u30af\u30b7\u30e7\u30f3\u3092\u5b9f\u884c\u3057\u305f\u6642\u306b\u4e00\u5ea6\u30de\u30a6\u30b9\u306e\u30dc\u30bf\u30f3\u3092\u96e2\u3055\u306a\u3044\u3068\u9078\u629e\u3067\u304d\u306a\u3044\u3088\u3046\u306b\u3059\u308b\u305f\u3081
  private boolean helpFlag = false;                        //\u30d8\u30eb\u30d7\u3092\u8868\u793a\u3059\u308b\u304b\u3069\u3046\u304b
  private boolean gameOverFlag;                            //\u30b2\u30fc\u30e0\u30aa\u30fc\u30d0\u30fc\u306e\u7ba1\u7406
  private boolean charterFlightFlag = false;               //\u30c1\u30e3\u30fc\u30bf\u30fc\u4fbf\u306b\u3088\u308b\u79fb\u52d5
  private boolean airliftFlag = false;                     //\u30a4\u30d9\u30f3\u30c8\u30ab\u30fc\u30c9"\u7a7a\u8f38"\u3092\u4f7f\u7528\u3057\u305f\u6642
  private boolean oneQuietNightFlag = false;               //\u30a4\u30d9\u30f3\u30c8\u30ab\u30fc\u30c9"\u9759\u304b\u306a\u591c"\u3092\u4f7f\u7528\u3057\u305f\u6642
  private boolean governmentGrantFlag = false;             //\u30a4\u30d9\u30f3\u30c8\u30ab\u30fc\u30c9"\u653f\u5e9c\u306e\u88dc\u52a9"\u3092\u4f7f\u7528\u3057\u305f\u6642
  
  private int[] infectionRateTrack = {2,2,2,3,3,4,4};      //\u611f\u67d3\u30ab\u30fc\u30c9\u306e\u30c9\u30ed\u30fc\u679a\u6570
  private int irtCnt = 0;                                  //\u2191\u7528\u306e\u5909\u6570
  
  private IntList playerDeck = new IntList();              //\u90fd\u9053\u5e9c\u770c( 0 to 46 ) \u30a4\u30d9\u30f3\u30c8\u30ab\u30fc\u30c9( 47 to 51 ) \u30a8\u30d4\u30c7\u30c3\u30af\u30ab\u30fc\u30c9( 52 to 57 )
  private IntList playerDiscardPile = new IntList();       //\u30d7\u30ec\u30a4\u30e4\u30fc\u30ab\u30fc\u30c9\u6368\u3066\u672d
  private IntList infectionDeck = new IntList();           //\u611f\u67d3\u30ab\u30fc\u30c9\u30c7\u30c3\u30ad
  private IntList infectionDiscardPile = new IntList();    //\u611f\u67d3\u30ab\u30fc\u30c9\u6368\u3066\u672d
  private IntList exclusionInfectionCard = new IntList();  //\u9664\u5916\u3055\u308c\u305f\u611f\u67d3\u30ab\u30fc\u30c9\u7528
  private int drawCardNum = 0;                             // 2 - drawCardNum = \u30bf\u30fc\u30f3\u7d42\u4e86\u6642\u30d7\u30ec\u30a4\u30e4\u30fc\u304c\u30c9\u30ed\u30fc\u3059\u308b\u679a\u6570
  private String epidemicCardName;                         //\u30a8\u30d4\u30c7\u30df\u30c3\u30af\u30ab\u30fc\u30c9\u51e6\u7406\u3067\u5f15\u3044\u305f\u611f\u67d3\u30ab\u30fc\u30c9\u306e\u540d\u524d
  
  private IntList researchStationList = new IntList();     //\u8abf\u67fb\u57fa\u5730\u304c\u7f6e\u3044\u3066\u3042\u308b\u5834\u6240\u306e\u914d\u5217
  private int[] cureMarkersFlag = {0,0,0,0};               //\u4f5c\u6210\u3057\u305f\u6cbb\u7642\u85ac\u306e\u30d5\u30e9\u30b0 0:\u672a 1:\u6e08 2:\u6839\u7d76
  private int discoverCureCnt = 5;                         //\u6cbb\u7642\u85ac\u3092\u4f5c\u6210\u3059\u308b\u306e\u306b\u5fc5\u8981\u306a\u90fd\u5e02\u30ab\u30fc\u30c9\u306e\u679a\u6570
  
  private IntList outbreakLoopList = new IntList();        //\u30a2\u30a6\u30c8\u30d6\u30ec\u30a4\u30af\u51e6\u7406\u7528
  private int outBreakCount = 0;                           //\u30a2\u30a6\u30c8\u30d6\u30ec\u30a4\u30af\u304c\u8d77\u3053\u3063\u305f\u56de\u6570
  
  private PVector centerPoint = new PVector();             //\u591a\u89d2\u5f62\u5224\u5b9a\u7528
  private ArrayList<PVector> hitLine = new ArrayList<PVector>();

  GAME_STATUS(){}
  
  public void resetSetting(){
    phase = PHASE.GAME;
    draws.targetPlayerNo = MAIN_TURN.n;
    draws.selectCard.clear();
    draws.displayCard.clear();
    draws.selectPlayerNo.clear();
    draws.displayPlayer.clear();
    charterFlightFlag = false;
    airliftFlag = false;
    governmentGrantFlag = false;
    draws.scrollCnt = 0;
  }
  
  public void gameOverSettingReset(){
    playerNames.clear();
    charLst.clear();
    epidemicCardNumber = 0;
    playerNumber = 0;
    
    playerDeck.clear();
    playerDiscardPile.clear();
    infectionDeck.clear();
    infectionDiscardPile.clear();
    exclusionInfectionCard.clear();
    drawCardNum = 0;
    epidemicCardName = "";
    researchStationList.clear();
    for ( int i = 0; i < cureMarkersFlag.length; i++ ){
      cureMarkersFlag[i] = 0;
    }
    irtCnt = 0;
    outBreakCount = 0;
  }
  
  public void update(){
    //\u4f4d\u7f6e\u66f4\u65b0
    centerPoint.set( mouseX, mouseY );      //\u4e2d\u5fc3\u70b9
    hitLine.get( 0 ).set( mouseX, 0      ); //\u4e0a
    hitLine.get( 1 ).set( mouseX, height ); //\u4e0b
    hitLine.get( 2 ).set( 0     , mouseY ); //\u5de6
    hitLine.get( 3 ).set( width , mouseY ); //\u53f3
  }
}
class Draws{                       
  private float infectionCardDrawFrame = 0;                //\u611f\u67d3\u30ab\u30fc\u30c9\u3092\u30c9\u30ed\u30fc\u3057\u305f\u6642\u306e\u6f14\u51fa\u7528
  private float discoverCureDrawFrame = 0;                 //"\u6cbb\u7642\u85ac\u306e\u4f5c\u6210"\u30c9\u30ed\u30fc\u3057\u305f\u6642\u306e\u6f14\u51fa\u7528
  private boolean discoverCureSuccessFlag;                 //\u6cbb\u7642\u85ac\u3092\u4f5c\u6210\u3067\u304d\u305f\u6642\u306btrue
  private float outBreakDrawFrame = 0;                     //\u30a2\u30a6\u30c8\u30d6\u30ec\u30a4\u30af\u304c\u8d77\u3053\u3063\u305f\u6642\u306e\u6f14\u51fa\u7528
  private StringList outBreakDrawName = new StringList();  //\u30a2\u30a6\u30c8\u30d6\u30ec\u30a4\u30af\u304c\u8d77\u3053\u3063\u305f\u5834\u6240\u306e\u540d\u524d
  private float epidemicCardDrawFrame = 0;                 //\u30a8\u30d4\u30c7\u30df\u30c3\u30af\u30ab\u30fc\u30c9\u3092\u30c9\u30ed\u30fc\u3057\u305f\u6642\u306e\u6f14\u51fa\u7528
  
  private int targetPlayerNo;                              //\u5bfe\u8c61\u306e\u30d7\u30ec\u30a4\u30e4\u30fc
  private IntList displayCard = new IntList();             //\u30ab\u30fc\u30c9\u8868\u793a\u7528\u914d\u5217
  private IntList selectCard = new IntList();              //\u9078\u629e\u3057\u305f\u30ab\u30fc\u30c9\u3092\u683c\u7d0d\u3059\u308b\u914d\u5217
  private int scrollCnt = 0;                               //\u9078\u629e\u3057\u305f\u30ab\u30fc\u30c9\u679a\u6570\u304c8\u679a\u3092\u8d85\u3048\u308b\u6642\u306b\u4f7f\u7528
  private IntList displayPlayer = new IntList();           //\u30d7\u30ec\u30a4\u30e4\u30fc\u8868\u793a\u7528\u914d\u5217
  private IntList selectPlayerNo = new IntList();          //\u9078\u629e\u3057\u305f\u30d7\u30ec\u30a4\u30e4\u30fc\u3092\u683c\u7d0d\u3059\u308b\u914d\u5217
  
  Draws(){}
  
  public void gameMainDraw(){
    int i, j, cnt = 0, pos = -1, ret;
    float x, y;
    player p = players.get( MAIN_TURN.n );
    prefecture t;
    IntList lst = new IntList();  //\u81ea\u52d5\u8eca\u307e\u305f\u306f\u8239\u306b\u3088\u308b\u79fb\u52d5
    getList( todoufuken.get( p.position ).adjacent, lst );
    
    //\u30dc\u30fc\u30c9\u753b\u50cf
    tint( WHITE );
    boardImgSet.get( 0 ).display();
    
    ////\u79fb\u52d5\u53ef\u80fd\u306a\u30de\u30b9\u306e\u8272\u3092\u5909\u66f4
    strokeWeight( 2 );
    if( !gameStatus.charterFlightFlag && !gameStatus.airliftFlag  & !gameStatus.governmentGrantFlag ){
      //\u76f4\u884c\u4fbf\u306b\u3088\u308b\u79fb\u52d5
      for( i = 0; i < p.cards.size(); i++ ){
        if( p.position != p.cards.get(i) ){
          lst.append( p.cards.get( i ) );
        }
      }
      //\u30b7\u30e3\u30c8\u30eb\u4fbf\u306b\u3088\u308b\u79fb\u52d5
      if( searchList( gameStatus.researchStationList, p.position ) ){
        for( i = 0; i < gameStatus.researchStationList.size(); i++ ){
          if( p.position != gameStatus.researchStationList.get( i ) ){
            lst.append( gameStatus.researchStationList.get( i ) );
          }
        }
      }
    }else{
      //\u30c1\u30e3\u30fc\u30bf\u30fc\u4fbf\u306b\u3088\u308b\u79fb\u52d5\u3001\u30a4\u30d9\u30f3\u30c8\u30ab\u30fc\u30c9"\u7a7a\u8f38"
      lst.clear();
      lst = setList( todoufukenNum );
    }
    
    //\u30de\u30b9\u63cf\u5199
    for( i = 0; i < todoufuken.size(); i++ ){
      t = todoufuken.get( i );
      if( searchList( lst, i ) ){
        ret = t.display( true );
      }else{
        ret = t.display( false ); 
      }
      if( ret != -1 ){
        pos = ret; 
      }
    }
    
    //\u90fd\u9053\u5e9c\u770c\u4e0a\u306b\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u8868\u793a
    for( player pp : players ){
      pp.display();
    }
    
    //\u75c5\u539f\u4f53\u63cf\u5199
    for( prefecture tt : todoufuken ){
      tt.pathogenDisplay(); 
    }
    
    ////\u53f3\u4e0b\u306b\u30de\u30b9\u306e\u60c5\u5831\u3092\u5165\u308c\u308b
    //\u30ab\u30fc\u30bd\u30eb\u304c\u30de\u30b9\u306e\u4e0a\u306b\u306a\u3044\u306a\u3089\u30bf\u30fc\u30f3\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u4f4d\u7f6e\u60c5\u5831\u3092\u30bb\u30c3\u30c8
    if( pos == -1 ){
      pos = p.position;
    }
    t = todoufuken.get( pos );
    //\u53f3\u4e0b\u306b\u767d\u67a0\u3092\u8868\u793a
    noStroke();
    fill( WHITE, 100 );
    rect( 7.5f * bs.x, 3 * bs.y, 1.5f * bs.x, 3.5f * bs.y );
    //\u9078\u629e\u4e2d\u306e\u30de\u30b9\u306e\u540d\u524d\u306e\u8868\u793a
    float tsx = TEXT_SIZE * 3.7f, rSize = TEXT_SIZE * 1.5f;
    textSize( tsx );
    stroke( BLACK );
    strokeWeight( 3 );
    for( i = 2; i >= 0; i-- ){
      fill( textColorPattern[i] );
      text( t.name, 7.875f * bs.x + i, 3.25f * bs.y + i );
    }
    //\u75c5\u539f\u4f53\u306e\u8868\u793a
    x = 7.5f; y = 4;
    for( i = 0; i < t.pathogenCnt.length; i++ ){
      fill( pathogenColorPattern[i] );
      for( j = 0; j < t.pathogenCnt[i]; j++ ){
        rect( x * bs.x + ( j + 0.5f ) * rSize, y * bs.y + rSize, rSize, rSize );
      }
      y+=0.5f;
    }
    //\u8abf\u67fb\u57fa\u5730\u306e\u8868\u793a
    if( t.researchStation == true ){
      boardImgSet.get(3).display();
    }
    //\u30c6\u30ad\u30b9\u30c8\u30b5\u30a4\u30ba\u3092\u5143\u306b\u623b\u3059
    textSize( TEXT_SIZE );
        
    ////\u5de6\u4e0a\u306b\u30a2\u30af\u30b7\u30e7\u30f3\u30ab\u30a6\u30f3\u30c8\u3001\u611f\u67d3\u30ab\u30fc\u30c9\u3001\u30d7\u30ec\u30a4\u30e4\u30fc\u30ab\u30fc\u30c9\u60c5\u5831\u3092\u53cd\u6620
    tint( WHITE, 100 );
    //\u30a2\u30af\u30b7\u30e7\u30f3\u30ab\u30a6\u30f3\u30c8
    numbers.get( ACTION_COUNT ).display( 0, 0, bs.x, bs.y );
    //\u611f\u67d3\u30ab\u30fc\u30c9\u306e\u30c9\u30ed\u30fc\u679a\u6570
    numbers.get( gameStatus.infectionRateTrack[gameStatus.irtCnt] ).display( 0, bs.y, bs.x, bs.y );
    //\u30a2\u30a6\u30c8\u30d6\u30ec\u30a4\u30af\u56de\u6570
    if( gameStatus.outBreakCount <= 8 ){
      numbers.get( gameStatus.outBreakCount ).display( 0, bs.y * 2, bs.x, bs.y );
    }
    //\u611f\u67d3\u30ab\u30fc\u30c9
    textSize( TEXT_SIZE * 1.5f );
    boardImgSet.get( 1 ).display();
    cnt = 0; y = 0.5f; x = 1.25f;
    for( i = 0; i < todoufuken.size() - eventCardNum; i++ ){
      if( searchList( gameStatus.infectionDiscardPile, i ) ){
        fill( BLACK );
      }else if( searchList( gameStatus.exclusionInfectionCard, i ) ){
        fill( cGRAY );
      }else{
        fill( WHITE );
      }
      text( todoufuken.get(i).name, x * bs.x, y * TEXT_SIZE * 1.5f );
      y++;
      cnt++;
      if( cnt == 24 ){
        y = 0.5f; x+=0.5f;
      }
    }
    //\u30d7\u30ec\u30a4\u30e4\u30fc\u30ab\u30fc\u30c9
    boardImgSet.get( 2 ).display();
    cnt = 0;  y = 0.5f;  x+=0.5f;
    for( i = 0; i < todoufuken.size(); i++ ){
      if( searchList( gameStatus.playerDiscardPile, i ) ){
        fill( BLACK ); 
      }else{
        fill( WHITE );
      }

      text( todoufuken.get( i ).name, x * bs.x + TEXT_SIZE, y * TEXT_SIZE * 1.5f );
      cnt++;
      y++;
      if( cnt == 24 ){
        y = 0.5f; x += 0.5f;
      }else if( cnt == 47 ){
        y = 0.5f; x += 0.5f;
      }
    }
    textSize( TEXT_SIZE );
    
    //0:\u30c1\u30e3\u30fc\u30bf\u30fc\u4fbf\u306b\u3088\u308b\u79fb\u52d5\u30001:\u8abf\u67fb\u57fa\u5730\u306e\u8a2d\u7f6e 2:\u6cbb\u7642\u85ac\u306e\u767a\u898b 3:\u77e5\u8b58\u306e\u5171\u6709 4:\u30a4\u30d9\u30f3\u30c8\u30ab\u30fc\u30c9 5:\u7279\u6b8a\u6280\u80fd
    ImageSet images;
    for( i = 0; i < playerActionImgSet.size(); i++ ){
      images = playerActionImgSet.get( i );
      
      if( images.isHit() ){
        tint( YELLOW, 100 );
      }else{
        noTint();
      }
      if( i != playerActionImgSet.size() - 1 || p.playerSkillFlag ){
        images.display();
      }
    }
    noTint();
    
    //\u6cbb\u7642\u85ac\u306e\u767a\u898b
    for( i = 0; i < TreatDiseases.size(); i++ ){
      if( gameStatus.cureMarkersFlag[i] >= 1 ){
        images = cureMarkers.get( i );
      }else{
        images = TreatDiseases.get( i );
      }
      if( images.isHit() ){
        tint( YELLOW, 100 );
      }else{
        noTint();
      }
      images.display();
      if( gameStatus.cureMarkersFlag[i] == 2 ){
        strokeWeight( 5 );
        line( images.x, images.y, images.x + images.w, images.y + images.h );
        strokeWeight( 1 );
      }
    }
    noTint();
    
    //\u30a2\u30a6\u30c8\u30d6\u30ec\u30a4\u30af\u3001\u6cbb\u7642\u85ac\u306e\u4f5c\u6210\u3001\u611f\u67d3\u30ab\u30fc\u30c9\u3092\u30c9\u30ed\u30fc\u3001\u30a8\u30d4\u30c7\u30df\u30c3\u30af\u30ab\u30fc\u30c9\u7528\u306e\u63cf\u5199
    if( outBreakDrawFrame > 0 || discoverCureDrawFrame > 0 || infectionCardDrawFrame > 0 || epidemicCardDrawFrame > 0 ){
      specialProductionDraw();
    }else if( gameOverCheck() != GAME_OVER.CONTINUE ){  //\u30b2\u30fc\u30e0\u7d42\u4e86\u306e\u30c1\u30a7\u30c3\u30af
      phase = PHASE.GAME_OVER; 
    }
    
  }//gameBoardDraw
  
  public void specialProductionDraw(){
    int i;
    float x = 0, y = height * 0.5f, ofstX = 1, ofstY = 0;
    
    stroke( BLACK );
    if( epidemicCardDrawFrame-- > 0 ){  //\u30a8\u30d4\u30c7\u30df\u30c3\u30af\u30ab\u30fc\u30c9\u30c9\u30ed\u30fc\u6642\u306e\u63cf\u5199
      fill( GREEN, 128 );
      rect( 0, 0, width, height );
      fill( BLACK );
      textSize( TEXT_SIZE * 5 );
      text( "\u30a8\u30d4\u30c7\u30df\u30c3\u30af\u30ab\u30fc\u30c9\u3092\u30c9\u30ed\u30fc\u3057\u307e\u3057\u305f", width * 0.5f, height * 0.25f );
      text( gameStatus.epidemicCardName + "\u306b\u75c5\u539f\u4f53\u304c3\u3064\u7f6e\u304b\u308c\u307e\u3059", width * 0.5f, height * 0.5f );
      text( "\u611f\u67d3\u7387\u304c" + gameStatus.infectionRateTrack[gameStatus.irtCnt] + "\u306b\u306a\u308a\u307e\u3057\u305f", width * 0.5f, height * 0.75f );
    }else if( outBreakDrawFrame-- > 0 ){  //\u30a2\u30a6\u30c8\u30d6\u30ec\u30a4\u30af\u6642\u306e\u63cf\u5199
      x = width / ( ( outBreakDrawName.size() + 3 ) % 4 + 2 );
      fill( RED, 128 );
      rect( 0, 0, width, height );
      fill( BLACK );
      textSize( TEXT_SIZE * 5 );
      text( "\u30a2\u30a6\u30c8\u30d6\u30ec\u30a4\u30af\u304c\u767a\u751f\u3057\u307e\u3057\u305f", width * 0.5f, height * 0.25f );
      for( i = 0; i < outBreakDrawName.size(); i++ ){
        text( outBreakDrawName.get(i), x * ofstX ,y + ofstY );
        ofstX += 1;
        if( i == 3 ){
          ofstX = 1; ofstY = height * 0.25f;
        }
      }
    }else if( discoverCureDrawFrame-- > 0 ){  //\u6cbb\u7642\u85ac\u306e\u4f5c\u6210\u6642\u306e\u63cf\u5199
      int colRect, colTxt;
      String str;
      if( discoverCureSuccessFlag ){
        colRect = WHITE;
        colTxt = BLACK;
        str = "\u6cbb\u7642\u85ac\u3092\u4f5c\u6210\u3057\u307e\u3057\u305f";
      }else{
        colRect = BLACK;
        colTxt = WHITE;
        str = "\u6cbb\u7642\u85ac\u3092\u4f5c\u6210\u3059\u308b\u3053\u3068\u304c\u3067\u304d\u307e\u305b\u3093";
      }
      fill( colRect, 100 );
      rect( width * 0.25f, height  * 0.25f, width * 0.5f, height * 0.5f );
      fill( colTxt );
      textSize( TEXT_SIZE * 2.5f );
      text( str, width * 0.5f, height * 0.5f );
    }else if( infectionCardDrawFrame-- > 0 ){  //\u611f\u67d3\u30ab\u30fc\u30c9\u3092\u30c9\u30ed\u30fc\u3057\u305f\u6642\u306e\u63cf\u5199
      fill( DARKBLUE, 128 );
      rect( 0, 0, width, height );
      fill( WHITE );
      textSize( TEXT_SIZE * 2.5f );
      text( "\u75c5\u539f\u4f531\u3064\u3092\u4ee5\u4e0b\u306e\u90fd\u9053\u5e9c\u770c\u306b\u30bb\u30c3\u30c8\u3057\u307e\u3059", width * 0.5f, height * 0.375f );
      textSize( TEXT_SIZE * 5 );
      x = 3;
      for( i = 0; i < 2; i++ ){
        text( todoufuken.get( gameStatus.infectionDiscardPile.get( gameStatus.infectionDiscardPile.size() - ( i + 1 ) ) ).name, x * bs.x, height * 0.5f );
        x += 3;
      }
    }
    textSize( TEXT_SIZE );
    
    if( outBreakDrawFrame <= 0 ){
      outBreakDrawName.clear(); 
    }
  }//specialProductionDraw
  
  public void cardSelectDraw(){
    int x = 0, y = 3, i, loopCnt;
    float cardX = bs.x * 1.125f, cardY = bs.y * 1.125f, cardW = bs.x, cardH = bs.y * 2;
    IntList display = new IntList();
    prefecture t;
    boolean hitFlag = false;
    
    //\u6587\u5b57\u30b5\u30a4\u30ba\u30921.5\u500d\u306b
    textSize( TEXT_SIZE * 1.5f );
    
    //\u4e2d\u592e\u306b\u767d\u80cc\u666f
    fill( WHITE, 100 );
    rect( width / 4, height / 4, width / 2, height / 2 );
    
    ////\u8868\u793a\u3059\u308b\u30ab\u30fc\u30c9\u306e\u60c5\u5831
    //8\u679a\u3092\u8d85\u3048\u305f\u6642
    if( displayCard.size() > 8 ){
      //\u4e0a\u4e0b\u30dc\u30bf\u30f3
      loopCnt = 8;
      tint( WHITE, 100 );
      for( i = 0; i < jouge.size(); i++ ){
        jouge.get( i ).display();
        if( jouge.get( 0 ).isHit() && scrollCnt > 0 && pressButton() ){
          scrollCnt--;
        }else if( jouge.get( 1 ).isHit() && displayCard.size() > ( scrollCnt + 1 ) * 8 && pressButton()  ){
          scrollCnt++;
        }
      }
      if( displayCard.size() - scrollCnt * 8 < 8 ){
        loopCnt = displayCard.size() - scrollCnt * 8;
      }
      for( i = 0; i < loopCnt; i++ ){
         display.append( displayCard.get( i + ( scrollCnt * 8 ) ) );
      }
      noTint();
    }else if( displayCard.size() >= 1 ){  //\u30c7\u30a3\u30b9\u30d7\u30ec\u30a4\u7528\u306e\u914d\u5217\u306b\u6570\u5b57\u304c\u5b58\u5728\u3059\u308b\u6642
      display = displayCard;
    }else{
      display = players.get( targetPlayerNo ).cards;
    }
    
    for( i = 0; i < display.size(); i++ ){
      t = todoufuken.get( display.get( i ) );
      
      //\u9078\u629e\u3057\u305f\u30ab\u30fc\u30c9\u306e\u30ea\u30b9\u30c8\u306b\u5b58\u5728\u3057\u3066\u3044\u308b\u6642\u3001\u975e\u8868\u793a
      if( searchList( selectCard, t.position ) == false ){
        //\u9078\u629e\u3057\u3066\u3044\u308b\u304b\u3069\u3046\u304b
        if( rectHit( ( x + 2.5f ) * cardX - cardW / 2, y * cardY - cardH / 2, cardW, cardH ) ){
          hitFlag = true;
          //\u9078\u629e\u4e2d\u306b\u30dc\u30bf\u30f3\u304c\u62bc\u3055\u308c\u305f\u6642
          if( pressButton() ){
            selectCard.append( t.position );
            break;
          }
        }
        
        //\u624b\u672d\u306e\u30ab\u30fc\u30c9\u8868\u793a
        //\u9078\u629e:\u767d \u975e\u9078\u629e:\u305d\u308c\u305e\u308c\u306e\u8272
        if( hitFlag == true ){
          fill( WHITE ); 
        }else{
          fill( pathogenColorPattern[t.col], 200 );
        }
        rect( ( x + 2.5f ) * cardX - cardW / 2, y * cardY - cardH / 2, cardW, cardH );
        
        //\u540d\u524d\u306e\u8868\u793a
        //\u9078\u629e:\u9ed2 \u975e\u9078\u629e:\u767d
        if( hitFlag == true ){
          fill( BLACK );
        }else{
          fill( WHITE );
        }
        text( t.name, ( x + 2.5f ) * cardX, y * cardY );
      }
      
      //\u6b21
      x++;
      if( i == 3 ){
        y = 5; x = 0;
      }
      hitFlag = false;
    }//for
    
    //\u6587\u5b57\u30b5\u30a4\u30ba\u3092\u5143\u306b\u623b\u3059
    textSize( TEXT_SIZE );
    
  }//cardSelectDraw
  
  public void playerSelectDraw(){
    float x = 0, ofst = 0, add = 0;
    float cardW = bs.x, cardH = bs.y * 2;
    boolean flag;
    
    switch( displayPlayer.size() ){
      case 1:  x = width * 0.5f; ofst = 1; add = 0;   break;
      case 2:  x = bs.x;        ofst = 3; add = 3;   break;
      case 3:  x = bs.x;        ofst = 3; add = 1.5f; break;
      case 4:  x = bs.x;        ofst = 3; add = 1;   break;
    }
    
    //\u767d\u80cc\u666f
    fill( WHITE, 100 );
    rect( width * 0.25f, height * 0.25f, width * 0.5f, height * 0.5f );
    textSize( TEXT_SIZE * 2.5f );
    
    for( int i = 0; i < displayPlayer.size(); i++ ){
      if( searchList( selectPlayerNo, i ) == false ){
        if( rectHit( ofst * x - cardW * 0.5f, height * 0.5f - cardH * 0.5f, cardW, cardH ) ){
          flag = true;
          if( pressButton() ){
            selectPlayerNo.append( displayPlayer.get( i ) );
            break;
          }//if
        }else{
          flag = false;
        }
        if( flag ){
          fill( WHITE );
        }else{
          fill( players.get( displayPlayer.get(i) ).col, 100 );
        }
        rect( ofst * x - cardW * 0.5f, height * 0.5f - cardH * 0.5f, cardW, cardH );
      
        if( flag ){
          fill( BLACK );
        }else{
          fill( WHITE );
        }
        text( players.get( displayPlayer.get(i) ).name, ofst * x, height * 0.5f );
      }
      ofst+=add;
    }//for    
  }//playerSelectDraw
  
  public void helpDraw(){
    int i;
    StringList str = new StringList();
    float wid = 1.125f * bs.x, ts = TEXT_SIZE * 2.5f;
    boolean flag = false;
    
    //\u53f3\u4e0a\u306b?\u30de\u30fc\u30af
    fill( WHITE );
    rect( width - RECT_SIZE * 0.5f, 0, RECT_SIZE * 0.5f, RECT_SIZE * 0.5f );
    fill( BLACK );
    textSize( TEXT_SIZE * 2 );
    text( "?", width - RECT_SIZE * 0.25f, RECT_SIZE * 0.25f );
    
    if( gameStatus.helpFlag ){
      fill( WHITE, 200 );
      rect( 0, 0, width, height );
      
      str = makeStringList( TEXT_SIZE * 2, rule );
      
      //\u30d7\u30ec\u30a4\u30e4\u30fc\u30ab\u30fc\u30c9
      for( i = 0; i < players.size(); i++ ){
        player p = players.get( i );
        if( rectHit( 7.875f * bs.x - ( p.no * wid ), 7 * bs.y, wid, 2 * bs.y ) ){
          fill( p.col );
          rect( 7.875f * bs.x - ( p.no * wid ), 7 * bs.y, wid, 2 * bs.y );
          flag = true;
          
          switch( p.role ){
            case CONTINGENCY_PLANNER:   //\u5371\u6a5f\u7ba1\u7406\u5b98
              str = makeStringList( ts,"\u5371\u6a5f\u7ba1\u7406\u5b98","\u30071\u30a2\u30af\u30b7\u30e7\u30f3\u3092\u6d88\u8cbb\u3057\u3066\u6368\u3066\u672d\u306e\u30a4\u30d9\u30f3\u30c8\u30ab\u30fc\u30c9\u30921\u679a\u56de\u53ce\u3067\u304d\u307e\u3059\u3002" );
            break;
            case DISPATCHER:            //\u901a\u4fe1\u6307\u4ee4\u54e1
              str = makeStringList( ts,"\u901a\u4fe1\u6307\u4ee4\u54e1","\u30071\u30a2\u30af\u30b7\u30e7\u30f3\u3092\u6d88\u8cbb\u3057\u3066\u4ed6\u306e\u30d7\u30ec\u30a4\u30e4\u30fc\u3092\u5225\u306e\u30d7\u30ec\u30a4\u30e4\u30fc\u304c\u3044\u308b\u90fd\u5e02\u306b\u79fb\u52d5\u3059\u308b\u3053\u3068\u304c\u3067\u304d\u307e\u3059\u3002" );
            break;
            case OPERATIONS_EXPERT:     //\u4f5c\u6226\u30a8\u30ad\u30b9\u30d1\u30fc\u30c8
              str = makeStringList( ts,"\u4f5c\u6226\u30a8\u30ad\u30b9\u30d1\u30fc\u30c8","\u3007\u4efb\u610f\u306e\u90fd\u5e02\u30ab\u30fc\u30c9\u3092\u6368\u3066\u3066\u81ea\u5206\u306e\u3044\u308b\u90fd\u5e02\u306b\u8abf\u67fb\u57fa\u5730\u3092\u8a2d\u7f6e\u3059\u308b\u3053\u3068\u304c\u3067\u304d\u307e\u3059\u3002","\u3007\u81ea\u5206\u304c\u8abf\u67fb\u57fa\u5730\u306b\u3044\u308b\u6642\u3001\u4efb\u610f\u306e\u90fd\u5e02\u30ab\u30fc\u30c9\u3092\u6368\u3066\u3066\u4efb\u610f\u306e\u90fd\u5e02\u306b\u79fb\u52d5\u3067\u304d\u307e\u3059\u3002" );
            break;
            case MEDIC:                 //\u885b\u751f\u5175
              str = makeStringList( ts,"\u885b\u751f\u5175","\u3007\u611f\u67d3\u306e\u6cbb\u7642\u3092\u884c\u3046\u6642\u306b\u540c\u3058\u8272\u306e\u75c5\u539f\u4f53\u3092\u5168\u3066\u53d6\u308a\u9664\u304f\u3053\u3068\u304c\u3067\u304d\u307e\u3059\u3002","\u3007\u6cbb\u7642\u85ac\u304c\u4f5c\u6210\u3055\u308c\u3066\u3044\u3066\u3001\u79fb\u52d5\u5148\u306b\u305d\u306e\u8272\u306e\u75c5\u539f\u4f53\u304c\u3042\u308b\u6642\u306b\u30a2\u30af\u30b7\u30e7\u30f3\u3092\u6d88\u8cbb\u3057\u306a\u3044\u3067\u53d6\u308a\u9664\u304f\u3053\u3068\u304c\u3067\u304d\u307e\u3059\u3002" );
            break;
            case SCIENTIST:             //\u79d1\u5b66\u8005
              str = makeStringList( ts,"\u79d1\u5b66\u8005","\u3007\u540c\u3058\u8272\u306e\u90fd\u5e02\u30ab\u30fc\u30c94\u679a\u3067\u6cbb\u7642\u85ac\u3092\u4f5c\u6210\u3059\u308b\u3053\u3068\u304c\u3067\u304d\u307e\u3059\u3002" );
            break;
            case RESEARCHER:            //\u7814\u7a76\u54e1
              str = makeStringList( ts,"\u7814\u7a76\u54e1","\u30071\u30a2\u30af\u30b7\u30e7\u30f3\u3092\u6d88\u8cbb\u3057\u3066\u81ea\u5206\u306e\u624b\u672d\u306e\u90fd\u5e02\u30ab\u30fc\u30c91\u679a\u3092\u540c\u3058\u90fd\u5e02\u306b\u3044\u308b\u30d7\u30ec\u30a4\u30e4\u30fc\u306b\u6e21\u3059\u3053\u3068\u304c\u3067\u304d\u307e\u3059\u3002" );
            break;
            case QUARANTINE_SPECIALIST: //\u691c\u75ab\u5b98
              str = makeStringList( ts,"\u691c\u75ab\u5b98","\u3007\u81ea\u5206\u304c\u3044\u308b\u90fd\u5e02\u3068\u305d\u306e\u96a3\u63a5\u306e\u90fd\u5e02\u306b\u75c5\u539f\u4f53\u304c\u7f6e\u304b\u308c\u308b\u3053\u3068\u3092\u9632\u304e\u307e\u3059\u3002" );
            break;
          }//switch
        }//if
      }//for
      
      for( i = 0; i < playerActionImgSet.size(); i++ ){
        if( playerActionImgSet.get(i).isHit() ){
          playerActionImgSet.get(i).display();
          flag = true;
          
          switch( i ){
            case 0: //\u30c1\u30e3\u30fc\u30bf\u30fc\u4fbf\u306b\u3088\u308b\u79fb\u52d5
              str = makeStringList( ts,"\u30c1\u30e3\u30fc\u30bf\u30fc\u4fbf\u306b\u3088\u308b\u79fb\u52d5","\u3007\u30d7\u30ec\u30a4\u30e4\u30fc\u304c\u73fe\u5728\u3044\u308b\u90fd\u5e02\u3068\u540c\u3058\u90fd\u5e02\u306e\u30ab\u30fc\u30c9\u3092\u6368\u3066\u308b\u3053\u3068\u3067\u4efb\u610f\u306e\u90fd\u5e02\u306b\u79fb\u52d5\u3059\u308b\u3053\u3068\u304c\u3067\u304d\u307e\u3059\u3002" );
            break;
            case 1: //\u8abf\u67fb\u57fa\u5730\u306e\u8a2d\u7f6e
              str = makeStringList( ts,"\u8abf\u67fb\u57fa\u5730\u306e\u8a2d\u7f6e","\u3007\u30d7\u30ec\u30a4\u30e4\u30fc\u304c\u73fe\u5728\u3044\u308b\u90fd\u5e02\u3068\u540c\u3058\u90fd\u5e02\u306e\u30ab\u30fc\u30c9\u3092\u6368\u3066\u308b\u3053\u3068\u3067\u305d\u306e\u90fd\u5e02\u306b\u8abf\u67fb\u57fa\u5730\u3092\u8a2d\u7f6e\u3057\u307e\u3059\u3002",
                "\u3007\u8abf\u67fb\u57fa\u5730\u306e\u3042\u308b\u90fd\u5e02\u3067\u306f\u6cbb\u7642\u85ac\u3092\u4f5c\u6210\u3059\u308b\u3053\u3068\u304c\u3067\u304d\u307e\u3059\u3002","\u3007\u8abf\u67fb\u57fa\u5730\u306e\u3042\u308b\u90fd\u5e02\u540c\u58eb\u3067\u79fb\u52d5\u3059\u308b\u3053\u3068\u304c\u3067\u304d\u307e\u3059\u3002" );
            break;
            case 2: //\u77e5\u8b58\u306e\u5171\u6709
              str = makeStringList( ts,"\u77e5\u8b58\u306e\u5171\u6709","\u3007\u540c\u3058\u90fd\u5e02\u306b\u3044\u308b\u30d7\u30ec\u30a4\u30e4\u30fc\u304b\u3089\u305d\u306e\u90fd\u5e02\u306e\u30ab\u30fc\u30c9\u3092\u53d7\u3051\u53d6\u308b\u3053\u3068\u304c\u3067\u304d\u307e\u3059\u3002","\u3007\u540c\u3058\u90fd\u5e02\u306b\u3044\u308b\u30d7\u30ec\u30a4\u30e4\u30fc\u306b\u305d\u306e\u90fd\u5e02\u306e\u30ab\u30fc\u30c9\u3092\u6e21\u3059\u3053\u3068\u304c\u3067\u304d\u307e\u3059\u3002" );
            break;
            case 3: //\u6cbb\u7642\u85ac\u306e\u767a\u898b
              str = makeStringList( ts,"\u6cbb\u7642\u85ac\u306e\u767a\u898b","\u3007\u8abf\u67fb\u57fa\u5730\u306e\u3042\u308b\u90fd\u5e02\u3067\u3001\u540c\u8272\u306e\u90fd\u5e02\u30ab\u30fc\u30c9\u30925\u679a(\u79d1\u5b66\u8005\u306f4\u679a)\u6368\u3066\u308b\u3053\u3068\u3067\u3001\u305d\u306e\u8272\u306e\u6cbb\u7642\u85ac\u3092\u4f5c\u6210\u3059\u308b\u3053\u3068\u304c\u3067\u304d\u307e\u3059\u3002",
              "\u3007\u6cbb\u7642\u85ac\u3068\u540c\u3058\u8272\u306e\u75c5\u539f\u4f53\u306f1\u5ea6\u306e\u611f\u67d3\u306e\u6cbb\u7642\u3067\u5168\u3066\u53d6\u308a\u9664\u304f\u3053\u3068\u304c\u3067\u304d\u307e\u3059\u3002");
            break;
            case 4: //\u30a4\u30d9\u30f3\u30c8\u30ab\u30fc\u30c9
              str = makeStringList( ts,"\u30a4\u30d9\u30f3\u30c8\u30ab\u30fc\u30c9","\u3007\u611f\u67d3\u30ab\u30fc\u30c9\u51e6\u7406\u6642\u4ee5\u5916\u3001\u4efb\u610f\u306e\u30bf\u30a4\u30df\u30f3\u30b0\u3067\u30a2\u30af\u30b7\u30e7\u30f3\u3092\u6d88\u8cbb\u305b\u305a\u306b\u767a\u52d5\u304c\u3067\u304d\u308b\u30ab\u30fc\u30c9\u3067\u3059\u3002",
                "\u30fb\u7a7a\u8f38\uff1a\u4efb\u610f\u306e\u30d7\u30ec\u30a4\u30e4\u30fc\u3092\u4efb\u610f\u306e\u90fd\u5e02\u306b\u79fb\u52d5\u3057\u307e\u3059\u3002",
                "\u30fb\u4e88\u6e2c\uff1a\u611f\u67d3\u30ab\u30fc\u30c9\u306e\u30c7\u30c3\u30ad\u306e\u4e0a\u304b\u30896\u679a\u306e\u30ab\u30fc\u30c9\u3092\u898b\u3066\u3001\u597d\u304d\u306a\u9806\u756a\u3067\u30c7\u30c3\u30ad\u306b\u623b\u3059\u3002",
                "\u30fb\u653f\u5e9c\u306e\u88dc\u52a9\uff1a\u4efb\u610f\u306e\u90fd\u5e02\u306b\u8abf\u67fb\u57fa\u5730\u3092\u8a2d\u7f6e\u3059\u308b\u3053\u3068\u304c\u3067\u304d\u307e\u3059\u3002",
                "\u30fb\u9759\u304b\u306a\u591c\uff1a\u767a\u52d5\u30bf\u30fc\u30f3\u306e\u611f\u67d3\u30ab\u30fc\u30c9\u306e\u30c9\u30ed\u30fc\u3092\u884c\u3044\u307e\u305b\u3093\u3002",
                "\u30fb\u4eba\u53e3\u56de\u5fa9\uff1a\u611f\u67d3\u30ab\u30fc\u30c9\u306e\u6368\u3066\u672d\u306e\u30ab\u30fc\u30c91\u679a\u3092\u30b2\u30fc\u30e0\u304b\u3089\u53d6\u308a\u9664\u304f\u3002" );
            break;
            case 5: //\u7279\u6b8a\u6280\u80fd
              str = makeStringList( ts,"\u7279\u6b8a\u6280\u80fd","\u3007\u30a2\u30af\u30b7\u30e7\u30f3\u3092\u6d88\u8cbb\u3057\u3066\u767a\u52d5\u3059\u308b\u7279\u5225\u306a\u30b9\u30ad\u30eb\u3092\u6301\u3063\u3066\u3044\u308b\u5834\u5408\u3001\u4f7f\u3046\u3053\u3068\u304c\u3067\u304d\u307e\u3059\u3002" );
            break;
          }//switch
        }//if
      }//for

      for( i = 1; i < boardImgSet.size() - 1; i++ ){
        if( boardImgSet.get( i ).isHit() ){
          boardImgSet.get( i ).display();
          flag = true;
          
          switch( i ){
            case 1: //\u611f\u67d3\u30ab\u30fc\u30c9
              str = makeStringList( ts,"\u611f\u67d3\u30ab\u30fc\u30c9","\u3007\u4f7f\u7528\u6e08\u307f\u306e\u30ab\u30fc\u30c9\u306f\u9ed2\u8272\u3067\u8868\u793a\u3055\u308c\u307e\u3059\u3002","\u3007\u9664\u5916\u3055\u308c\u3066\u3044\u308b\u30ab\u30fc\u30c9\u306f\u7070\u8272\u3067\u6a19\u793a\u3055\u308c\u307e\u3059\u3002" );
            break;
            case 2: //\u30d7\u30ec\u30a4\u30e4\u30fc\u30ab\u30fc\u30c9
              str = makeStringList( ts,"\u30d7\u30ec\u30a4\u30e4\u30fc\u30ab\u30fc\u30c9","\u3007\u4f7f\u7528\u6e08\u307f\u306e\u30ab\u30fc\u30c9\u306f\u9ed2\u8272\u3067\u8868\u793a\u3055\u308c\u307e\u3059\u3002","\u3007\u9664\u5916\u3055\u308c\u3066\u3044\u308b\u30ab\u30fc\u30c9\u306f\u7070\u8272\u3067\u6a19\u793a\u3055\u308c\u307e\u3059\u3002" );
            break;
          }//switch
        }//if
      }//for
      
      for( i = 0; i < TreatDiseases.size(); i++ ){
        String colorName = "";
        if( TreatDiseases.get(i).isHit() ){
          TreatDiseases.get(i).display();
          flag = true;
          
          switch( i ){
            case 0: colorName = "\u9752";   break;
            case 1: colorName = "\u8d64";   break;
            case 2: colorName = "\u9752\u7dd1"; break;
            case 3: colorName = "\u7d2b";   break;
          }//switch
          str = makeStringList( ts,colorName + "\u8272\u75c5\u539f\u4f53\u306e\u611f\u67d3\u306e\u6cbb\u7642",
            "\u3007\u30d7\u30ec\u30a4\u30e4\u30fc\u304c\u73fe\u5728\u3044\u308b\u90fd\u5e02\u306e" + colorName + "\u8272\u75c5\u539f\u4f53\u30921\u3064\u53d6\u308a\u9664\u304f\u3053\u3068\u304c\u3067\u304d\u307e\u3059",
            "\u3007" + colorName + "\u8272\u306e\u6cbb\u7642\u85ac\u3092\u4f5c\u6210\u3059\u308b\u3068\u3001\u30d7\u30ec\u30a4\u30e4\u30fc\u304c\u73fe\u5728\u3044\u308b\u90fd\u5e02\u306e" + colorName + "\u8272\u75c5\u539f\u4f53\u3092\u5168\u3066\u53d6\u308a\u9664\u304f\u3053\u3068\u304c\u3067\u304d\u307e\u3059\u3002",
            "\u3007\u5168\u3066\u306e\u90fd\u5e02\u304b\u3089" + colorName + "\u8272\u75c5\u539f\u4f53\u304c\u53d6\u308a\u9664\u304b\u308c\u308b\u3068" + colorName + "\u8272\u306e\u90fd\u5e02\u306e\u611f\u67d3\u30ab\u30fc\u30c9\u3092\u5f15\u3044\u3066\u3082\u75c5\u539f\u4f53\u304c\u7f6e\u304b\u308c\u308b\u3053\u3068\u306f\u3042\u308a\u307e\u305b\u3093\u3002" );
        }//if
      }//for
      
      for( i = 0; i < 3; i++ ){
        if( rectHit( 0, i * bs.y , bs.x, bs.y ) ){
          flag = true;
          switch( i ){
            case 0: //\u30a2\u30af\u30b7\u30e7\u30f3\u30ab\u30a6\u30f3\u30c8
              str = makeStringList( ts,"\u30a2\u30af\u30b7\u30e7\u30f3","\u3007\u5b9f\u884c\u53ef\u80fd\u306a\u6b8b\u308a\u30a2\u30af\u30b7\u30e7\u30f3\u6570\u3067\u3059\u3002" );
              numbers.get( ACTION_COUNT ).display( 0, 0, bs.x, bs.y );
            break;
            case 1: //\u611f\u67d3\u7387
              str = makeStringList( ts,"\u611f\u67d3\u7387","\u3007\u30bf\u30fc\u30f3\u7d42\u4e86\u6642\u306b\u30c9\u30ed\u30fc\u3059\u308b\u611f\u67d3\u30ab\u30fc\u30c9\u306e\u679a\u6570\u3067\u3059\u3002","\u3007\u30a8\u30d4\u30c7\u30df\u30c3\u30af\u30ab\u30fc\u30c9\u3092\u30c9\u30ed\u30fc\u3057\u305f\u6642\u306b\u611f\u67d3\u7387\u304c\u9032\u884c\u3057\u307e\u3059\u3002" );
              numbers.get( gameStatus.infectionRateTrack[gameStatus.irtCnt] ).display( 0, bs.y, bs.x, bs.y );
            break;
            case 2: //\u30a2\u30a6\u30c8\u30d6\u30ec\u30a4\u30af\u56de\u6570
              str = makeStringList( ts,"\u30a2\u30a6\u30c8\u30d6\u30ec\u30a4\u30af","\u3007\u767a\u751f\u3057\u305f\u30a2\u30a6\u30c8\u30d6\u30ec\u30a4\u30af\u306e\u6570\u3067\u3059\u3002","\u30078\u56de\u30a2\u30a6\u30c8\u30d6\u30ec\u30a4\u30af\u304c\u8d77\u3053\u308b\u3068\u6557\u5317\u3057\u307e\u3059\u3002" );
              numbers.get( gameStatus.outBreakCount ).display( 0, 2 * bs.y, bs.x, bs.y );
            break;
          }//switch
        }//if
      }//for
      
      //\u30c6\u30ad\u30b9\u30c8\u306e\u8868\u793a
      helpText( str, flag );
      
    }//if
  }//helpDraw
  
  public void helpText( StringList str, boolean flag ){
    textSize( TEXT_SIZE * 5 );
    fill( BLACK );
    if( flag ){
      text( str.get( 0 ), width * 0.5f, height * 0.125f );
      textAlign( LEFT, CENTER );
      textSize( TEXT_SIZE * 2.5f );
      for( int i = 1; i < str.size(); i++ ){ 
        text( str.get( i ), width * 0.125f, height * 0.125f + i * TEXT_SIZE * 5 );
      }
    }else{
      text( str.get( 0 ), width * 0.5f, height * 0.125f );
      textAlign( LEFT, CENTER );
      textSize( TEXT_SIZE * 2 );
      for( int i = 1; i < str.size(); i++ ){
        text( str.get( i ), width * 0.125f, height * 0.125f + i * TEXT_SIZE * 2 );
      }
    }//if
    str.clear();
    textAlign( CENTER, CENTER );
  }//helpText
  
  public void gameInputDraw(){
    int i,j,k;
    float ts = ( width + height ) / 18;
    float x = width * 0.25f, y  = height * 0.375f;
    
    background( DARKBLUE );
    for( i = 0; i < 3; i++ ){ 
      textSize( ts );
      fill( textColorPattern[i] );
      text( "PANDEMIC", width * 0.5f + ( i * 5 ), ts + ( i * 5 ) );
      textSize( TEXT_SIZE );
      
      textAlign( LEFT, CENTER );
      switch( phase ){
        case PLAYER_NAME_INPUT:
          for( j = 0; j < playerNames.size() + 1; j++ ){
            String str = "";
            if( j == playerNames.size() ){
              for( k = 0; k < charLst.size(); k++ ){
                str += charLst.get(k);
              }
            }else{
              str = playerNames.get(j);
            }
            text( "\u25a1\u30d7\u30ec\u30a4\u30e4\u30fc" + ( j + 1 ) + "\u306e\u540d\u524d: " + str, x, y + TEXT_SIZE * ( j + 2 ) + i * 2 );
          }
        case PLAYER_NUMBER_INPUT:
          text( "\u25a1\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u4eba\u6570: " + playerNumber + " (2 \uff5e 4)", x, y + TEXT_SIZE + i * 2 );
        case GAME_DIFFICULTY_INPUT:
          text( "\u25a1\u30a8\u30d4\u30c7\u30df\u30c3\u30af\u30ab\u30fc\u30c9\u306e\u679a\u6570: " + epidemicCardNumber + " (4 \uff5e 6)", x, y + i * 2 );
        break;
        default:
      }
      textAlign( CENTER, CENTER );
    }
  }
  
  public void gameOverDraw(){
    int col;
    float ts = TEXT_SIZE * 5;
    StringList str = new StringList();
    
    if( gameStatus.gameOverFlag ){
      col = YELLOW;
      str = makeStringList( ts,"\u52dd\u5229", "\u5168\u3066\u306e\u75c5\u539f\u4f53\u3092\u6392\u9664\u3057\u305f!" );
    }else{
      col = DARKBLUE;
      str = makeStringList( ts,"\u6557\u5317", "\u4eba\u985e\u306f\u6ec5\u4ea1\u3057\u307e\u3057\u305f" );
    }
    fill( col, 200 );
    rect( 0, 0, width, height );
    
    textSize( ts );
    fill( BLACK );
    for( int i = 0; i < str.size(); i++ ){ 
      text( str.get( i ), width * 0.5f, height * 0.25f + i * TEXT_SIZE * 5 );
    }
    str.clear();
  }//gameOverDraw
}
//\u90fd\u9053\u5e9c\u770c
ArrayList<prefecture> todoufuken = new ArrayList<prefecture>();
//\u30d7\u30ec\u30a4\u30e4\u30fc
ArrayList<player> players = new ArrayList<player>();
//0:\u30dc\u30fc\u30c9 1:\u611f\u67d3\u30ab\u30fc\u30c9 2:\u30d7\u30ec\u30a4\u30e4\u30fc\u30ab\u30fc\u30c9 3:\u8abf\u67fb\u57fa\u5730
ArrayList<ImageSet> boardImgSet = new ArrayList<ImageSet>();
//0:\u30c1\u30e3\u30fc\u30bf\u30fc\u4fbf\u306b\u3088\u308b\u79fb\u52d5\u30001:\u8abf\u67fb\u57fa\u5730\u306e\u8a2d\u7f6e 2:\u6cbb\u7642\u85ac\u306e\u767a\u898b 3:\u77e5\u8b58\u306e\u5171\u6709 4:\u30a4\u30d9\u30f3\u30c8\u30ab\u30fc\u30c9
ArrayList<ImageSet> playerActionImgSet = new ArrayList<ImageSet>();
//\u6cbb\u7642\u85ac 0:\u9752 1:\u8d64 2:\u9752\u7dd1 3:\u7d2b
ArrayList<ImageSet> TreatDiseases = new ArrayList<ImageSet>();
ArrayList<ImageSet> cureMarkers = new ArrayList<ImageSet>();
//\u6570\u5b57
ArrayList<ImageSet> numbers = new ArrayList<ImageSet>();
//\u4e0a\u4e0b
ArrayList<ImageSet> jouge = new ArrayList<ImageSet>();

final int RED      = color( 255, 0  , 0   );
final int BLUE     = color( 100, 100, 230 );
final int GREEN    = color( 0  , 255, 0   );
final int DARKBLUE = color( 0  , 0  , 139 );
final int PURPLE   = color( 150, 0  , 150 );
final int YELLOW   = color( 255, 255, 100 );
final int LIGHTSEAGREEN = color( 32 , 178, 170 );
final int BLACK    = color( 0  , 0  , 0   );
final int WHITE    = color( 255, 255, 255 );
final int ENJI     = color( 179, 66 , 74  );
final int cGRAY    = color( 190, 190, 190 );
final int[] pathogenColorPattern = {BLUE, ENJI, LIGHTSEAGREEN, PURPLE, cGRAY};
final int[] textColorPattern = {BLACK, cGRAY, WHITE};

PVector bs;                    //\u4f4d\u7f6e\u8abf\u6574\u7528
Draws draws;                   //\u80cc\u666f\u7b49\u306e\u63cf\u5199\u7528
PHASE phase;                   //\u30d5\u30a7\u30fc\u30ba\u7ba1\u7406
Phases phases;                 //\u30d5\u30a7\u30fc\u30ba\u306e\u4e2d\u8eab\u7ba1\u7406
GAME_STATUS gameStatus;        //\u30b2\u30fc\u30e0\u30b9\u30c6\u30fc\u30bf\u30b9\u7528

String[] rule;                 //\u30d8\u30eb\u30d7\u7528

float TEXT_SIZE;               //\u30c6\u30ad\u30b9\u30c8\u306e\u30b5\u30a4\u30ba
float ELLIPSE_SIZE;            //ellipse\u306e\u30b5\u30a4\u30ba
float RECT_SIZE;               //rect\u306e\u30b5\u30a4\u30ba
int ACTION_COUNT = 4;          //\u30a2\u30af\u30b7\u30e7\u30f3\u306e\u30ab\u30a6\u30f3\u30c8

final int CURE = -999;         //\u6cbb\u7642\u85ac\u7528

final int todoufukenNum = 47;  //\u90fd\u9053\u5e9c\u770c\u306e\u6570
final int eventCardNum = 5;    //\u30a4\u30d9\u30f3\u30c8\u30ab\u30fc\u30c9\u306e\u6570

final int AIRLIFT = 47;              //\u7a7a\u8f38
final int FORECAST = 48;             //\u4e88\u6e2c
final int GOVERNMENT_GRANT = 49;     //\u653f\u5e9c\u306e\u88dc\u52a9
final int ONE_QUIET_NIGHT = 50;      //\u9759\u304b\u306a\u591c
final int RESILIENT_POPULATION = 51; //\u4eba\u53e3\u56de\u5fa9

enum GAME_OVER{
  LOSE,      //\u6557\u5317
  CONTINUE,  //\u7d9a\u884c
  WIN;       //\u52dd\u5229
}

enum PHASE{
  GAME_DIFFICULTY_INPUT,     //\u30b2\u30fc\u30e0\u96e3\u6613\u5ea6\u5165\u529b
  PLAYER_NUMBER_INPUT,       //\u4eba\u6570\u5165\u529b
  PLAYER_NAME_INPUT,         //\u540d\u524d\u5165\u529b
  GAME,                      //\u30b2\u30fc\u30e0
  HAND_LIMIT,                //\u624b\u672d\u3092\u6368\u3066\u308b
  CHARTER_FLIGHT,            //\u30c1\u30e3\u30fc\u30bf\u30fc\u4fbf\u306b\u3088\u308b\u79fb\u52d5
  DISCOVER_A_CURE,           //\u6cbb\u7642\u85ac\u306e\u767a\u898b
  SHARE_KNOWLEDGE,           //\u77e5\u8b58\u306e\u5171\u6709
  BUILD_A_RESEARCH_STATION,  //\u4f5c\u6226\u30a8\u30ad\u30b9\u30d1\u30fc\u30c8\u306b\u3088\u308b\u8abf\u67fb\u57fa\u5730\u306e\u8a2d\u7f6e
  SPECIAL_SKILL,             //\u7279\u6b8a\u6280\u80fd
  EVENT_CARD,                //\u30a4\u30d9\u30f3\u30c8\u30ab\u30fc\u30c9
  GAME_OVER;                 //\u30b2\u30fc\u30e0\u7d42\u4e86
}

enum TURN{
  PLAYER1(0),   //\u30d7\u30ec\u30a4\u30e4\u30fc1
  PLAYER2(1),   //\u30d7\u30ec\u30a4\u30e4\u30fc2
  PLAYER3(2),   //\u30d7\u30ec\u30a4\u30e4\u30fc3
  PLAYER4(3);   //\u30d7\u30ec\u30a4\u30e4\u30fc4
  final int n;
  private TURN( int n ){
    this.n = n;
  }
}
TURN MAIN_TURN = TURN.PLAYER1;

enum ROLES{                       //\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u5f79\u8077
  CONTINGENCY_PLANNER(true,0,255,255),  //\u5371\u6a5f\u7ba1\u7406\u5b98
  DISPATCHER(true,255,0,255),           //\u901a\u4fe1\u6307\u4ee4\u54e1
  OPERATIONS_EXPERT(true,170,204,59),   //\u4f5c\u6226\u30a8\u30ad\u30b9\u30d1\u30fc\u30c8
  MEDIC(false,230,121,40),              //\u885b\u751f\u5175
  SCIENTIST(false,210,209,192),         //\u79d1\u5b66\u8005
  RESEARCHER(true,153,76,0),            //\u7814\u7a76\u54e1
  QUARANTINE_SPECIALIST(false,0,128,0); //\u691c\u75ab\u5b98
  
  final int[] roleColor = new int[3];
  final boolean playerSkillFlag;
  private ROLES( boolean playerSkillFlag, int... roleColor ){
    this.playerSkillFlag = playerSkillFlag;
    arrayCopy( roleColor, this.roleColor );
  }
}

StringList playerNames = new StringList();  //\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u540d\u524d
StringList charLst = new StringList();      //\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u540d\u524d\u5165\u529b\u7528\u30ea\u30b9\u30c8
int epidemicCardNumber = 0;                 //\u30a8\u30d4\u30c7\u30df\u30c3\u30af\u30ab\u30fc\u30c9\u306e\u6570
int playerNumber = 0;                       //\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u4eba\u6570
public void specialSkillProcess(){
  player p = players.get( MAIN_TURN.n );
  switch( p.role ){
    case CONTINGENCY_PLANNER:
      if( draws.displayCard.size() <= 0 ){
        //\u30a4\u30d9\u30f3\u30c8\u30ab\u30fc\u30c9\u3092\u8868\u793a\u3059\u308b
        for( int i = 0; i < gameStatus.playerDiscardPile.size(); i++ ){
          int num = gameStatus.playerDiscardPile.get( i );
          if( AIRLIFT <= num && num <= RESILIENT_POPULATION ){
            draws.displayCard.append( num );
          }
        }//for
        //\u30a4\u30d9\u30f3\u30c8\u30ab\u30fc\u30c9\u304c\u5b58\u5728\u3057\u306a\u3044\u306a\u3089\u30ea\u30bb\u30c3\u30c8
        if( draws.displayCard.size() <= 0 ){
          gameStatus.resetSetting(); 
        }
      }else{
        //\u30a4\u30d9\u30f3\u30c8\u30ab\u30fc\u30c9\u3092\u8868\u793a\u3059\u308b
        draws.cardSelectDraw();
        //\u30a4\u30d9\u30f3\u30c8\u30ab\u30fc\u30c9\u3092\u9078\u629e\u3057\u305f\u3089\u3001\u305d\u306e\u30ab\u30fc\u30c9\u3092\u624b\u672d\u306b\u52a0\u3048\u308b
        if( draws.selectCard.size() >= 1 ){
          p.setCard( draws.selectCard.get( 0 ) );
          for( int i = 0; i < gameStatus.playerDiscardPile.size(); i++ ){
            if( draws.selectCard.get( 0 ) == gameStatus.playerDiscardPile.get( i ) ){
              gameStatus.playerDiscardPile.remove( i );
              break;
            }
          }
          ACTION_COUNT--;
          gameStatus.resetSetting();
        }
      }//if
    break;
    case DISPATCHER:
      draws.playerSelectDraw();
      if( draws.selectPlayerNo.size() >= 2 ){
        player sel = players.get( draws.selectPlayerNo.get( 0 ) );
        player pos = players.get( draws.selectPlayerNo.get( 1 ) );
        
        if( sel.position != pos.position ){
          sel.move( pos.position );
          ACTION_COUNT--;
        }
        gameStatus.resetSetting();
      }
    break;
    case OPERATIONS_EXPERT:
      if( searchList( gameStatus.researchStationList, players.get( MAIN_TURN.n ).position ) ){  //\u8abf\u67fb\u57fa\u5730\u306e\u3042\u308b\u90fd\u5e02\u306b\u3044\u308b\u6642
        draws.cardSelectDraw();
        if( draws.selectCard.size() >= 1 ){
          gameStatus.charterFlightFlag = true;
          phase = PHASE.GAME;
        }
      }else{  //\u8abf\u67fb\u57fa\u5730\u306e\u306a\u3044\u90fd\u5e02\u306b\u3044\u308b\u6642
        draws.cardSelectDraw();
        if( draws.selectCard.size() >= 1 ){
          p = players.get( draws.targetPlayerNo );
          prefecture t = todoufuken.get( p.position );
          
          p.removeCard( draws.selectCard.get( 0 ) );
          t.setResearchStation();
          
          ACTION_COUNT--;
          gameStatus.resetSetting();
        }
      }
    break;
    case RESEARCHER:
      if( draws.displayPlayer.size() <= 0 ){
        gameStatus.resetSetting();
      }else if( draws.selectPlayerNo.size() <= 0 ){
        draws.playerSelectDraw();
      }else if( draws.selectPlayerNo.size() >= 1 && draws.selectCard.size() <= 0 ){
        draws.targetPlayerNo = MAIN_TURN.n;
        draws.cardSelectDraw();
      }else if( draws.selectCard.size() >= 1 ){
        player src = players.get( MAIN_TURN.n );
        player dest = players.get( draws.selectPlayerNo.get( 0 ) );
        
        //src\u306e\u30ab\u30fc\u30c9\u3092dest\u306b\u30bb\u30c3\u30c8\u3059\u308b
        dest.setCard( src.removeCard( draws.selectCard.get( 0 ) ) );
        
        ACTION_COUNT--;
        gameStatus.resetSetting();
      }
    break;
    default:
      gameStatus.resetSetting();
    break;
  }
}//specialSkillProcess

//\u30a4\u30d9\u30f3\u30c8\u30ab\u30fc\u30c9\u306e\u51e6\u7406
public void eventCardProcess(){
  int i;
  
  if( draws.selectCard.size() <= 0 ){
    draws.cardSelectDraw();
  }else{
    switch( draws.selectCard.get( 0 ) ){
      case AIRLIFT:  //\u7a7a\u8f38
        if( !gameStatus.airliftFlag ){
          if( draws.displayPlayer.size() <= 0 ){
            for( i = 0; i < players.size(); i++ ){
              draws.displayPlayer.append( i );
            }
          }
          draws.playerSelectDraw();
          if( draws.selectPlayerNo.size() >= 1 ){
            draws.targetPlayerNo = draws.selectPlayerNo.get( 0 );
            gameStatus.airliftFlag = true;
            phase = PHASE.GAME;
          }
        }
      break;
      case FORECAST:  //\u4e88\u6e2c
        if( draws.displayCard.size() <= 0 ){
          //\u8868\u793a\u3059\u308b\u30ab\u30fc\u30c9\u306e\u60c5\u5831\u3092\u5165\u308c\u308b
          for( i = 1; i <= 6; i++ ){
            draws.displayCard.append( gameStatus.infectionDeck.get( gameStatus.infectionDeck.size() - i ) );
          }
          gameStatus.pressFlag = false;
        }else{
          draws.cardSelectDraw();
          if( draws.selectCard.size() >= 7 ){  //1\u679a\u76ee\u30a4\u30d9\u30f3\u30c8\u30ab\u30fc\u30c9 + 6\u679a
            for( i = 1; i <= 6; i++ ){
              gameStatus.infectionDeck.set( gameStatus.infectionDeck.size() - i, draws.selectCard.get( i ) );
            }
            players.get( draws.targetPlayerNo ).removeCard( draws.selectCard.get( 0 ) );
            gameStatus.resetSetting();
          }
        }
      break;
      case GOVERNMENT_GRANT:  //\u653f\u5e9c\u306e\u88dc\u52a9
        gameStatus.governmentGrantFlag = true;
        for( prefecture tt : todoufuken ){
          if( tt.isHit() && pressButton() ){
            if( gameStatus.governmentGrantFlag && tt.setResearchStation() ){
              players.get( draws.targetPlayerNo ).removeCard( draws.selectCard.get( 0 ) );
              gameStatus.resetSetting();
            }
          }
        }
      break;
      case ONE_QUIET_NIGHT:  //\u9759\u304b\u306a\u591c
        gameStatus.oneQuietNightFlag = true;
        players.get( draws.targetPlayerNo ).removeCard( draws.selectCard.get( 0 ) );
        gameStatus.resetSetting();
      break;
      case RESILIENT_POPULATION:  //\u4eba\u53e3\u56de\u5fa9
        if( draws.displayCard.size() <= 0 ){
          for( i = 0; i < gameStatus.infectionDiscardPile.size(); i++ ){
            draws.displayCard.append( gameStatus.infectionDiscardPile.get( i ) );
          }
          draws.displayCard.sort();
        }
        draws.cardSelectDraw();
        if( draws.selectCard.size() >= 2 ){  //1\u679a\u76ee\u30a4\u30d9\u30f3\u30c8\u30ab\u30fc\u30c9 + \u9078\u629e\u3057\u305f1\u679a
          for( i = 0; i < gameStatus.infectionDiscardPile.size(); i++ ){
            if( draws.selectCard.get( 1 ) == gameStatus.infectionDiscardPile.get( i ) ){
              gameStatus.exclusionInfectionCard.append( gameStatus.infectionDiscardPile.remove( i ) );
            }
          }
          players.get( draws.targetPlayerNo ).removeCard( draws.selectCard.get( 0 ) );
          gameStatus.resetSetting();
        }
      break;
      default:
        gameStatus.resetSetting();
      break;
    }
  }
}//eventCardProcess

//\u77e5\u8b58\u306e\u5171\u6709
public void shareKnowledgeProcess(){
  draws.playerSelectDraw();
  if( draws.selectPlayerNo.size() >= 1 ){
    player src = players.get( phases.shareKnowledgeSrc );
    player dest = players.get( draws.selectPlayerNo.get( 0 ) );
    
    //src\u306e\u30ab\u30fc\u30c9\u3092dest\u306b\u30bb\u30c3\u30c8\u3059\u308b
    dest.setCard( src.removeCard( src.position ) );
    
    ACTION_COUNT--;
    gameStatus.resetSetting();
  }
}//shareKnowledgeProcess

public void handLimitProcess(){
  draws.cardSelectDraw();
  if( draws.selectCard.size() >= 1 ){
    player p = players.get( draws.targetPlayerNo );
    p.removeCard( draws.selectCard.get( 0 ) );
    
    gameStatus.resetSetting();
  }
}//handLimitProcess

public void discoverCureProcess(){
  draws.cardSelectDraw();
  if( draws.selectCard.size() >= gameStatus.discoverCureCnt ){  //5\u679a\u306e\u30ab\u30fc\u30c9\u3092\u9078\u629e\u3057\u305f\u6642\u3001role\u304cSCIENTIST\u306a\u30894\u679a
    int i;
    player p = players.get( draws.targetPlayerNo );
    //\u6700\u521d\u306e\u30ab\u30e9\u30fc\u3092col\u306b
    int col = todoufuken.get( draws.selectCard.get( 0 ) ).col;
    //1\u3064\u76ee\u3068\u305d\u306e\u4ed6\u3092\u6bd4\u3079\u3066\u3044\u304f
    for( i = 1; i < draws.selectCard.size(); i++ ){
      //\u9055\u3063\u305f\u3089\u30d6\u30ec\u30a4\u30af
      if( col != todoufuken.get( draws.selectCard.get( i ) ).col ){
        break;
      }
    }
    //\u6700\u5f8c\u307e\u3067\u30eb\u30fc\u30d7(=\u8272\u304c\u540c\u3058)\u3057\u305f\u3089\u6cbb\u7642\u85ac\u3092\u4f5c\u6210
    draws.discoverCureSuccessFlag = false;
    if( i >= draws.selectCard.size() ){
      IntList rmvLst = new IntList();
      for( i = 0; i < draws.selectCard.size(); i++ ){
        rmvLst.append( draws.selectCard.get( i ) );
      }
      for( i = 0; i < draws.selectCard.size(); i++ ){
        p.removeCard( rmvLst.get( i ) );
      }
      draws.discoverCureSuccessFlag = true;
      gameStatus.cureMarkersFlag[col] = 1;
      ACTION_COUNT--;
    }
    draws.discoverCureDrawFrame = frameRate * 2;
    gameStatus.resetSetting();
  }
}//discoverCureProcess 

public GAME_OVER gameOverCheck(){
  int i, pathogenTotalCnt = 0, cnt = 0;
  boolean flag = false;
  
  //\u52dd\u5229
  for( i = 0; i < gameStatus.cureMarkersFlag.length; i++ ){
    if( gameStatus.cureMarkersFlag[i] >= 1 ){
      cnt++;
    }
  }
  //\u5168\u3066\u306e\u6cbb\u7642\u85ac\u3092\u4f5c\u6210\u3057\u305f\u6642\u3001\u52dd\u5229
  if( cnt >= gameStatus.cureMarkersFlag.length ){
    gameStatus.gameOverFlag = true;
    return GAME_OVER.WIN;
  }
  
  ////\u6557\u5317
  //\u7f6e\u304b\u308c\u3066\u3044\u308b\u5404\u8272\u306e\u75c5\u539f\u4f53\u306e\u6570\u3092\u5408\u8a08\u3059\u308b
  for( i = 0; i < 4; i++ ){
    for( prefecture t : todoufuken ){
      pathogenTotalCnt += t.pathogenCnt[i];
    }
    if( pathogenTotalCnt >= 24 ){
      flag = true;
      break;
    }
    pathogenTotalCnt = 0;
  }
  //\u30d1\u30f3\u30c7\u30df\u30c3\u30af\u56de\u6570\u304c8\u4ee5\u4e0a \u304b \u7f6e\u304b\u308c\u3066\u3044\u308b\u5404\u8272\u306e\u75c5\u539f\u4f53\u6570\u304c24\u4ee5\u4e0a \u304b \u30d7\u30ec\u30a4\u30e4\u30fc\u30c7\u30c3\u30ad\u306e\u6570\u304c0\u4ee5\u4e0b\u306b\u306a\u3063\u305f\u6642\u3001\u6557\u5317
  if( gameStatus.outBreakCount >= 8 || flag || gameStatus.playerDeck.size() <= 0 ){
    gameStatus.gameOverFlag = false;
    return GAME_OVER.LOSE;
  }
  
  //\u4ed6
  return GAME_OVER.CONTINUE;
}//gameOverCheck

public void turnChange(){
  int i, card;
  player p = players.get( MAIN_TURN.n );
  
  //\u30d7\u30ec\u30a4\u30e4\u30fc\u306b\u30ab\u30fc\u30c9\u3092\u30bb\u30c3\u30c8
  for( i = gameStatus.drawCardNum; i < 2; i++ ){
    card = gameStatus.playerDeck.remove( gameStatus.playerDeck.size() - 1 );
    if( card > 51 ){
      epidemicCardProcess();
    }else{
      p.setCard( card );
    }
    if( phase == PHASE.HAND_LIMIT ){
      gameStatus.drawCardNum = i + 1;
      return;
    }
  }
  gameStatus.drawCardNum = 0;
  ACTION_COUNT = 4;
  
  //\u611f\u67d3\u30ab\u30fc\u30c9\u3092\u30c9\u30ed\u30fc
  if( !gameStatus.oneQuietNightFlag ){
    for( i = 0; i < gameStatus.infectionRateTrack[gameStatus.irtCnt]; i++ ){
      card = removeAndAppendList( gameStatus.infectionDiscardPile, gameStatus.infectionDeck, gameStatus.infectionDeck.size() - 1 );
      addPathogenProcess( card, todoufuken.get( card ).col, 1 );
      gameStatus.outbreakLoopList.clear();
    }
    draws.infectionCardDrawFrame = frameRate * 2;
  }else{
    gameStatus.oneQuietNightFlag = false;
  }
  
  //\u6b21\u306e\u30d7\u30ec\u30a4\u30e4\u30fc\u306b\u30bf\u30fc\u30f3\u3092\u30c1\u30a7\u30f3\u30b8
  if( MAIN_TURN == TURN.PLAYER1 ){
    MAIN_TURN = TURN.PLAYER2;
    draws.targetPlayerNo = MAIN_TURN.n;
    return;
  }
  if( MAIN_TURN == TURN.PLAYER2 && players.size() > 2 ){
    MAIN_TURN = TURN.PLAYER3;
    draws.targetPlayerNo = MAIN_TURN.n;
    return;
  }
  if( MAIN_TURN == TURN.PLAYER3 && players.size() > 3 ){
    MAIN_TURN = TURN.PLAYER4;
    draws.targetPlayerNo = MAIN_TURN.n;
    return;
  }
  MAIN_TURN = TURN.PLAYER1;
  draws.targetPlayerNo = MAIN_TURN.n;
}//turnChange

//rect\u578b\u306eHit\u7528
public boolean rectHit( float x, float y, float w, float h ){
  return ( x < mouseX && mouseX < x + w  && y < mouseY && mouseY < y + h );
}//rectHit

//\u30a8\u30d4\u30c7\u30df\u30c3\u30af\u30ab\u30fc\u30c9\u3092\u5f15\u3044\u305f\u6642\u306e\u51e6\u7406
public void epidemicCardProcess(){
  //\u611f\u67d3\u7387\u306e\u4e0a\u6607
  gameStatus.irtCnt++;
  
  //\u611f\u67d3
  int card = removeAndAppendList( gameStatus.infectionDiscardPile, gameStatus.infectionDeck, 0 );
  gameStatus.epidemicCardName = todoufuken.get( card ).name;
  addPathogenProcess( card, todoufuken.get( card ).col, 3 );
  gameStatus.outbreakLoopList.clear();
  
  //\u5ea6\u5408\u3044\u306e\u5897\u52a0
  gameStatus.infectionDiscardPile.shuffle();
  for( int i = 0; i < gameStatus.infectionDiscardPile.size(); i++ ){
    gameStatus.infectionDeck.append( gameStatus.infectionDiscardPile.get( i ) );
  }
  gameStatus.infectionDiscardPile.clear();
  
  draws.epidemicCardDrawFrame = frameRate * 2;
}//epidemicCardProcess

//\u75c5\u539f\u4f53\u3092\u90fd\u5e02\u306bsetNum\u500b\u8ffd\u52a0\u3059\u308b
//setNum\u304cCURE\u306e\u6642\u306b0\u306b\u3059\u308b
public void addPathogenProcess( int position, int colorsNumber, int setNum ){
  prefecture t = todoufuken.get( position );
  
  //\u6839\u7d76\u3055\u308c\u3066\u3044\u308b\u5834\u5408\u306freturn
  if( gameStatus.cureMarkersFlag[colorsNumber] == 2 ){
    return;
  }
  
  if( gameStatus.outbreakLoopList.hasValue( position ) == false && quarantineSpecialistHit( position, setNum ) ){
    if( t.pathogenCnt[colorsNumber] == 0 && setNum <= -1 ){
      ACTION_COUNT++;
      return;
    }
    
    if( setNum == CURE ){  //CURE\u306e\u66420
      t.pathogenCnt[colorsNumber] = 0;
      return;
    }
    
    if( t.pathogenCnt[colorsNumber] + setNum > 3 ){  //\u30a2\u30a6\u30c8\u30d6\u30ec\u30a4\u30af\u51e6\u7406
      draws.outBreakDrawFrame = frameRate * 2;
      draws.outBreakDrawName.append( t.name );
      gameStatus.outbreakLoopList.append( position );
      t.pathogenCnt[colorsNumber] = 3;
      gameStatus.outBreakCount++;
      for( int i = 0; i < t.adjacent.size(); i++ ){
        addPathogenProcess( t.adjacent.get(i), colorsNumber, 1 );
      }
      return;
    }//if
    
    t.pathogenCnt[colorsNumber] += setNum;
  }//if
}//addPathogenProcess

//\u5f79\u8077\u304c\u691c\u75ab\u5b98\u306e\u6642\u306e\u5224\u5b9a
public boolean quarantineSpecialistHit( int position, int cnt ){
  int pNo = -1;
  
  //\u7f6e\u304f\u500b\u6570\u304c-1\u4ee5\u4e0b(=\u53d6\u308a\u9664\u304f)\u304bCURE\u306e\u6642
  if( cnt <= -1 ){
    return true; 
  }
  
  //\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u4e2d\u306b\u691c\u75ab\u5b98\u304c\u3044\u308b\u304b\u3069\u3046\u304b
  for( player pp : players ){
    if( pp.role == ROLES.QUARANTINE_SPECIALIST ){
      pNo = pp.no;
      break;
    }
  }
  if( pNo == -1 ){
    return true; 
  }
  
  player p = players.get( pNo );
  prefecture t = todoufuken.get( p.position );
  if( p.position == position || searchList( t.adjacent, position ) ){ 
    return false;
  }else{
    return true;
  }
}//quarantineSpecialistHit

public void setupVar(){
  int i;
  float f;
  String[] names = new String[52];
  float[] xZahyou = new float[52], yZahyou = new float[52];
  int[] col = new int[52];
  
  bs = new PVector( width / 9, height / 9, 0 );  //\u5206\u5272\u30d6\u30ed\u30c3\u30af\u306e\u30b5\u30a4\u30ba
  ELLIPSE_SIZE  = ( width + height ) / 100;
  RECT_SIZE = ( width + height ) / 30;
  TEXT_SIZE = ( width + height ) / 60;  //\u4e00\u6642\u7684
  
  //\u5317\u6d77\u9053\u5730\u65b9
  names[0]  = "\u5317\u6d77\u9053"; xZahyou[0]  = width/1.23f;  yZahyou[0]  = height/5.95f;  col[0]  = 0;
  //\u6771\u5317\u5730\u65b9
  names[1]  = "\u9752\u68ee";   xZahyou[1]  = width/1.41f;  yZahyou[1]  = height/2.84f;  col[1]  = 0;
  names[2]  = "\u5ca9\u624b";   xZahyou[2]  = width/1.36f;  yZahyou[2]  = height/2.38f;  col[2]  = 0;
  names[3]  = "\u5bae\u57ce";   xZahyou[3]  = width/1.40f;  yZahyou[3]  = height/2.05f;  col[3]  = 0;
  names[4]  = "\u79cb\u7530";   xZahyou[4]  = width/1.46f;  yZahyou[4]  = height/2.41f;  col[4]  = 0;
  names[5]  = "\u5c71\u5f62";   xZahyou[5]  = width/1.49f;  yZahyou[5]  = height/2.02f;  col[5]  = 0;
  names[6]  = "\u798f\u5cf6";   xZahyou[6]  = width/1.48f;  yZahyou[6]  = height/1.78f;  col[6]  = 0;
  
  //\u95a2\u6771\u5730\u65b9
  names[7]  = "\u8328\u57ce";   xZahyou[7]  = width/1.46f;  yZahyou[7]  = height/1.57f;  col[7]  = 0;
  names[8]  = "\u6803\u6728";   xZahyou[8]  = width/1.52f;  yZahyou[8]  = height/1.66f;  col[8]  = 0;
  names[9]  = "\u7fa4\u99ac";   xZahyou[9]  = width/1.63f;  yZahyou[9]  = height/1.62f;  col[9]  = 0;
  names[10] = "\u57fc\u7389";   xZahyou[10] = width/1.60f;  yZahyou[10] = height/1.54f;  col[10] = 1;
  names[11] = "\u5343\u8449";   xZahyou[11] = width/1.46f;  yZahyou[11] = height/1.47f;  col[11] = 1;
  names[12] = "\u6771\u4eac";   xZahyou[12] = width/1.55f;  yZahyou[12] = height/1.50f;  col[12] = 1;
  names[13] = "\u795e\u5948\u5ddd"; xZahyou[13] = width/1.59f;  yZahyou[13] = height/1.45f;  col[13] = 1;
  //\u4e2d\u90e8\u5730\u65b9
  names[14] = "\u65b0\u6f5f";   xZahyou[14] = width/1.64f;  yZahyou[14] = height/1.79f;  col[14] = 0;
  names[15] = "\u5bcc\u5c71";   xZahyou[15] = width/1.92f;  yZahyou[15] = height/1.64f;  col[15] = 1;
  names[16] = "\u77f3\u5ddd";   xZahyou[16] = width/2.00f;  yZahyou[16] = height/1.73f;  col[16] = 1;
  names[17] = "\u798f\u4e95";   xZahyou[17] = width/2.14f;  yZahyou[17] = height/1.53f;  col[17] = 1;
  names[18] = "\u5c71\u68a8";   xZahyou[18] = width/1.70f;  yZahyou[18] = height/1.49f;  col[18] = 1;
  names[19] = "\u9577\u91ce";   xZahyou[19] = width/1.79f;  yZahyou[19] = height/1.56f;  col[19] = 1;
  names[20] = "\u5c90\u961c";   xZahyou[20] = width/1.97f;  yZahyou[20] = height/1.51f;  col[20] = 1;
  names[21] = "\u9759\u5ca1";   xZahyou[21] = width/1.79f;  yZahyou[21] = height/1.40f;  col[21] = 1;
  names[22] = "\u611b\u77e5";   xZahyou[22] = width/1.94f;  yZahyou[22] = height/1.40f;  col[22] = 1;
  
  //\u8fd1\u757f\u5730\u65b9
  names[23] = "\u4e09\u91cd";   xZahyou[23] = width/2.12f;  yZahyou[23] = height/1.36f;  col[23] = 2;
  names[24] = "\u6ecb\u8cc0";   xZahyou[24] = width/2.19f;  yZahyou[24] = height/1.44f;  col[24] = 2;
  names[25] = "\u4eac\u90fd";   xZahyou[25] = width/2.40f;  yZahyou[25] = height/1.44f;  col[25] = 2;
  names[26] = "\u5927\u962a";   xZahyou[26] = width/2.35f;  yZahyou[26] = height/1.36f;  col[26] = 2;
  names[27] = "\u5175\u5eab";   xZahyou[27] = width/2.61f;  yZahyou[27] = height/1.43f;  col[27] = 2;
  names[28] = "\u5948\u826f";   xZahyou[28] = width/2.25f;  yZahyou[28] = height/1.33f;  col[28] = 2;
  names[29] = "\u548c\u6b4c\u5c71"; xZahyou[29] = width/2.40f;  yZahyou[29] = height/1.29f;  col[29] = 2;
  //\u4e2d\u56fd\u5730\u65b9
  names[30] = "\u9ce5\u53d6";   xZahyou[30] = width/2.96f;  yZahyou[30] = height/1.45f;  col[30] = 2;
  names[31] = "\u5cf6\u6839";   xZahyou[31] = width/3.84f;  yZahyou[31] = height/1.41f;  col[31] = 2;
  names[32] = "\u5ca1\u5c71";   xZahyou[32] = width/3.02f;  yZahyou[32] = height/1.38f;  col[32] = 2;
  names[33] = "\u5e83\u5cf6";   xZahyou[33] = width/3.58f;  yZahyou[33] = height/1.36f;  col[33] = 2;
  names[34] = "\u5c71\u53e3";   xZahyou[34] = width/4.83f;  yZahyou[34] = height/1.32f;  col[34] = 2;
  
  //\u56db\u56fd\u5730\u65b9
  names[35] = "\u5fb3\u5cf6";   xZahyou[35] = width/2.81f;  yZahyou[35] = height/1.28f;  col[35] = 3;
  names[36] = "\u9999\u5ddd";   xZahyou[36] = width/2.92f;  yZahyou[36] = height/1.32f;  col[36] = 3;
  names[37] = "\u611b\u5a9b";   xZahyou[37] = width/3.54f;  yZahyou[37] = height/1.26f;  col[37] = 3;
  names[38] = "\u9ad8\u77e5";   xZahyou[38] = width/3.28f;  yZahyou[38] = height/1.24f;  col[38] = 3;
  //\u4e5d\u5dde\u30fb\u6c96\u7e04\u5730\u65b9
  names[39] = "\u798f\u5ca1";   xZahyou[39] = width/5.99f;  yZahyou[39] = height/1.26f;  col[39] = 3;
  names[40] = "\u4f50\u8cc0";   xZahyou[40] = width/7.53f;  yZahyou[40] = height/1.23f;  col[40] = 3;
  names[41] = "\u9577\u5d0e";   xZahyou[41] = width/8.13f;  yZahyou[41] = height/1.19f;  col[41] = 3;
  names[42] = "\u718a\u672c";   xZahyou[42] = width/5.95f;  yZahyou[42] = height/1.17f;  col[42] = 3;
  names[43] = "\u5927\u5206";   xZahyou[43] = width/5.02f;  yZahyou[43] = height/1.22f;  col[43] = 3;
  names[44] = "\u5bae\u5d0e";   xZahyou[44] = width/5.09f;  yZahyou[44] = height/1.14f;  col[44] = 3;
  names[45] = "\u9e7f\u5150\u5cf6"; xZahyou[45] = width/6.56f;  yZahyou[45] = height/1.11f;  col[45] = 3;
  names[46] = "\u6c96\u7e04";   xZahyou[46] = width/24.98f; yZahyou[46] = height/1.04f;  col[46] = 3;
  
  //\u30a4\u30d9\u30f3\u30c8\u30ab\u30fc\u30c9
  names[47] = "\u7a7a\u8f38";       xZahyou[47] = -100; yZahyou[47] = -100;  col[47] = 4;
  names[48] = "\u4e88\u6e2c";       xZahyou[48] = -100; yZahyou[48] = -100;  col[48] = 4;
  names[49] = "\u653f\u5e9c\u306e\u88dc\u52a9"; xZahyou[49] = -100; yZahyou[49] = -100;  col[49] = 4;
  names[50] = "\u9759\u304b\u306a\u591c";   xZahyou[50] = -100; yZahyou[50] = -100;  col[50] = 4;
  names[51] = "\u4eba\u53e3\u56de\u5fa9";   xZahyou[51] = -100; yZahyou[51] = -100;  col[51] = 4;
  
  //\u90fd\u9053\u5e9c\u770c\u306e\u8a2d\u5b9a
  todoufuken.clear();
  for( i = 0; i < todoufukenNum + eventCardNum; i++ ){
    todoufuken.add( new prefecture( names[i], i, xZahyou[i], yZahyou[i], wallSet(i), col[i], 0, adjacentSet(i), false ) );
  }
  todoufuken.get( 12 ).setResearchStation();  //\u6771\u4eac\u306b\u8abf\u67fb\u57fa\u5730\u3092\u8a2d\u5b9a
  
  ////\u753b\u50cf\u306e\u8aad\u307f\u8fbc\u307f
  boardImgSet.clear();
  playerActionImgSet.clear();
  TreatDiseases.clear();
  cureMarkers.clear();
  numbers.clear();
  jouge.clear();
  //0:\u30dc\u30fc\u30c9 1:\u611f\u67d3\u30ab\u30fc\u30c9 2:\u30d7\u30ec\u30a4\u30e4\u30fc\u30ab\u30fc\u30c9 3:\u8abf\u67fb\u57fa\u5730
  String[] nameBo = {"image/nihon.png","image/biohazard.png","image/redCross.png","image/researchStations.png"};
  float[] x ={ 0     , bs.x      , bs.x * 2   , bs.x * 8.5f };
  float[] y ={ 0     , 0         , 0          , bs.y * 3.5f };
  float[] w ={ width , bs.x      , bs.x * 1.75f, bs.x * 0.5f };
  float[] h ={ height, bs.y * 4.5f, bs.y * 4.5f , bs.y * 0.5f };
  for( i = 0; i < nameBo.length; i++ ){
    boardImgSet.add( new ImageSet( nameBo[i], x[i], y[i], w[i], h[i] ) );
  }
  
  //0:\u30c1\u30e3\u30fc\u30bf\u30fc\u4fbf\u306b\u3088\u308b\u79fb\u52d5\u30001:\u8abf\u67fb\u57fa\u5730\u306e\u8a2d\u7f6e 2:\u6cbb\u7642\u85ac\u306e\u767a\u898b 3:\u77e5\u8b58\u306e\u5171\u6709 4:\u30a4\u30d9\u30f3\u30c8\u30ab\u30fc\u30c9 5:\u7279\u6b8a\u6280\u80fd
  String[] namePa = {"image/CharterFlight.png","image/BuildResearchStation.png","image/ShareKnowledge.png","image/DiscoverCure.png","image/eventCard.png","image/specialSkill.png"};
  for( i = 0, f = 0; i < namePa.length - 1; i++ ){
    playerActionImgSet.add( new ImageSet( namePa[i], 8.5f * bs.x, ( 4.0f + f ) * bs.y, 0.5f * bs.x, 0.5f * bs.y ) );
    f+=0.5f;
  }
  playerActionImgSet.add( new ImageSet( namePa[i], 8.0f * bs.x, ( 3.5f + f ) * bs.y, 0.5f * bs.x, 0.5f * bs.y ) );
  
  //\u6cbb\u7642 0:\u9752 1:\u8d64 2:\u9ec4 3:\u7d2b
  String[] nameTd = {"image/TreatDiseaseBlue.png","image/TreatDiseaseRed.png","image/TreatDiseaseLightseagreen.png","image/TreatDiseasePurple.png"};
  for( i = 0, f = 0; i < nameTd.length; i++ ){
    TreatDiseases.add( new ImageSet( nameTd[i], 8.0f * bs.x, ( 4.0f + f ) * bs.y, 0.5f * bs.x, 0.5f * bs.y ) );
    f+=0.5f;
  }
  
  //\u6cbb\u7642\u85ac 0:\u9752 1:\u8d64 2:\u9ec4 3:\u7d2b
  String[] nameCm = {"image/CureMarkerRBlue.png","image/CureMarkerRed.png","image/CureMarkerLightseagreen.png","image/CureMarkerPurple.png"};
  for( i = 0, f = 0; i < nameCm.length; i++ ){
    cureMarkers.add( new ImageSet( nameCm[i], 8.0f * bs.x, ( 4.0f + f ) * bs.y, 0.5f * bs.x, 0.5f * bs.y ) );
    f+=0.5f;
  }
  
  //\u6570\u5b57
  for( i = 0; i < 9; i++ ){
    numbers.add( new ImageSet( "image/" + i + ".png" ) );
  }
  
  //\u4e0a\u4e0b
  String[] nameJouge = {"image/ue.png","image/sita.png"};
  for( i = 0; i < 2; i++ ){
    jouge.add( new ImageSet( nameJouge[i], 3 * width / 4, height / 4 + i * ( 0.5f * bs.y ), 0.5f * bs.x, 0.5f * bs.y ) );
  }  
  
  //\u611f\u67d3\u30ab\u30fc\u30c9\u306e\u8a2d\u5b9a
  gameStatus.infectionDeck = setList( todoufukenNum );
  
  //\u30d8\u30eb\u30d7\u7528\u306e\u30c6\u30ad\u30b9\u30c8
  rule = txtLoad( rule, "txt/rule.txt" );
  
  //\u5224\u5b9a\u7528
  gameStatus.hitLine.clear();
  gameStatus.hitLine.add( new PVector( mouseX, mouseY ) ); //\u4e0a
  gameStatus.hitLine.add( new PVector( mouseX, mouseY ) ); //\u4e0b
  gameStatus.hitLine.add( new PVector( mouseX, mouseY ) ); //\u5de6
  gameStatus.hitLine.add( new PVector( mouseX, mouseY ) ); //\u53f3
  
  //\u30d5\u30a7\u30fc\u30ba\u3092\u79fb\u884c
  phase = PHASE.GAME_DIFFICULTY_INPUT;
}//setupVar

//\u30c6\u30ad\u30b9\u30c8\u3092\u30ed\u30fc\u30c9\u3059\u308b
public String[] txtLoad( String[] txt, String path ){
  txt = loadStrings( path );
  txt[0] = txt[0].substring( 1, txt[0].length() );
  return txt;
}

//List\u304b\u3089remove\u3059\u308b\u5024\u3092append\u3059\u308bList\u306b\u5165\u308c\u3001\u305d\u306e\u5024\u3092\u8fd4\u5374
public int removeAndAppendList( IntList apnd, IntList rmv, int rmvNum ){
  int card = rmv.remove( rmvNum );
  apnd.append( card );
  
  return card;
}

//List\u306b\u6307\u5b9a\u3059\u308b\u7bc4\u56f2\u306e\u30e9\u30f3\u30c0\u30e0\u306a\u6570\u5b57\u3092\u30bb\u30c3\u30c8\u3059\u308b
public IntList setList( int num ){
  IntList lst = new IntList();
  for( int i = 0; i < num; i++ ){
    lst.append(i);
  }
  lst.shuffle();
  
  return lst;
}//setList

//List\u306b\u5bfe\u8c61\u306e\u6570\u5b57\u304c\u3042\u3063\u305f\u5834\u5408\u3001true\u3092\u8fd4\u5374
//List\u306b\u5bfe\u8c61\u306e\u6570\u5b57\u304c\u306a\u3044\u5834\u5408\u3001false\u3092\u8fd4\u5374
public boolean searchList( IntList lst ,int... num ){
  
  for( int i = 0; i < num.length; i++ ){
    if( lst.hasValue( num[i] ) == true ) {
      return true;
    }
  }
  return false;
  
}//searchList

//\u5f15\u6570\u306e\u914d\u5217\u304b\u3089IntList\u578b\u3092\u4f5c\u6210
public IntList initList( int... num ){
  IntList lst = new IntList();
  for( int i = 0; i < num.length; i++ ){
    lst.append( num[i] );
  }
  return lst;
}

//Sting\u578b\u306e\u914d\u5217\u304b\u3089StingList\u578b\u3092\u4f5c\u6210
public StringList makeStringList( float txtSize, String... strs ){
  int i, j;
  int loopNum, amari, perlineNum = floor( ( width * 0.75f ) / txtSize );
  StringList ret = new StringList();
  int st = 0, sp = 0;
  
  ret.append( strs[0] );
  for( i = 1; i < strs.length; i++ ){
    loopNum = floor( strs[i].length() / perlineNum );
    amari = strs[i].length() % perlineNum;
    if( amari == 0 ){
      loopNum--; 
    }
    st = 0;
    if( strs[i].length() < perlineNum ){
      sp = amari;
    }else{
      sp = perlineNum;
    }
    ret.append( strs[i].substring( st, sp ) );
    for( j = 0; j < loopNum; j++ ){
      st = sp;
      sp +=perlineNum;
      if( j == loopNum - 1 ){
        sp = st + amari;
      }
      ret.append( "\u3000" + strs[i].substring( st, sp ) );
    }
  }
  
  return ret;
}

public boolean pressButton(){
  if( gameStatus.pressFlag && mousePressed ){
    gameStatus.pressFlag = false;
    return true;
  }else{
    return false;
  }
}

public void getList( IntList src, IntList dest ){
  for( int i = 0; i < src.size(); i++ ){
    dest.append( src.get( i ) );
  }
}

//\u96a3\u63a5\u90fd\u9053\u5e9c\u770c\u306e\u8a2d\u5b9a
public IntList adjacentSet( int i ){
  switch( i ){
    case 0:  //\u5317\u6d77\u9053
      return initList(1);
    case 1:  //\u9752\u68ee
      return initList(0,2,4);
    case 2:  //\u5ca9\u624b
      return initList(1,3,4);
    case 3:  //\u5bae\u57ce
      return initList(2,4,5,6);
    case 4:  //\u79cb\u7530
      return initList(1,2,3,5);
    case 5:  //\u5c71\u5f62
      return initList(3,4,6,14);
    case 6:  //\u798f\u5cf6
      return initList(3,5,7,8,9,14);
    case 7:  //\u8328\u57ce
      return initList(6,8,10,11);
    case 8:  //\u6803\u6728
      return initList(6,7,9,10);
    case 9:  //\u57fc\u7389
      return initList(6,8,10,14,19);
    case 10:  //\u57fc\u7389
      return initList(7,8,9,11,12,18,19);
    case 11:  //\u5343\u8449
      return initList(7,10,12);
    case 12:  //\u6771\u4eac
      return initList(10,11,13,18);
    case 13:  //\u795e\u5948\u5ddd
      return initList(12,18,21);
    case 14:  //\u65b0\u6f5f
      return initList(5,6,9,15,19);
    case 15:  //\u5bcc\u5c71
      return initList(14,16,19,20);
    case 16:  //\u77f3\u5ddd
      return initList(15,17,20);
    case 17:  //\u798f\u4e95
      return initList(16,20,24,25);
    case 18:  //\u5c71\u68a8
      return initList(10,12,13,19,21);    
    case 19:  //\u9577\u91ce
      return initList(9,10,14,15,18,20,21,22);
    case 20:  //\u5c90\u961c
      return initList(15,16,17,19,22,23,24);    
    case 21:  //\u9759\u5ca1
      return initList(13,18,19,22);    
    case 22:  //\u611b\u77e5
      return initList(19,20,21,23);
    case 23:  //\u4e09\u91cd
      return initList(20,22,24,25,28,29);
    case 24:  //\u6ecb\u8cc0
      return initList(17,20,23,25);
    case 25:  //\u4eac\u90fd
      return initList(17,23,24,26,27,28);
    case 26:  //\u5927\u962a
      return initList(25,27,28,29);
    case 27:  //\u5175\u5eab
      return initList(25,26,30,32);
    case 28:  //\u5948\u826f
      return initList(23,25,26,29);
    case 29:  //\u548c\u6b4c\u5c71
      return initList(23,26,28);
    case 30:  //\u9ce5\u53d6
      return initList(27,31,32,33);
    case 31:  //\u5cf6\u6839
      return initList(30,33,34);
    case 32:  //\u5ca1\u5c71
      return initList(27,30,33,36);
    case 33:  //\u5e83\u5cf6
      return initList(30,31,32,34,37);
    case 34:  //\u5c71\u53e3
      return initList(31,33,39);
    case 35:  //\u5fb3\u5cf6
      return initList(36,37,38);
    case 36:  //\u9999\u5ddd
      return initList(32,35,37);
    case 37:  //\u611b\u5a9b
      return initList(33,35,36,38,43);
    case 38:  //\u9ad8\u77e5
      return initList(35,37);
    case 39:  //\u798f\u5ca1
      return initList(34,40,42,43);
    case 40:  //\u4f50\u8cc0
      return initList(39,41);
    case 41:  //\u9577\u5d0e
      return initList(40,46);
    case 42:  //\u718a\u672c
      return initList(39,43,44,45);
    case 43:  //\u5927\u5206
      return initList(37,39,42,44);
    case 44:  //\u5bae\u5d0e
      return initList(42,43,45);
    case 45:  //\u9e7f\u5150\u5cf6
      return initList(42,44,46);
    case 46:  //\u6c96\u7e04
      return initList(41,45);
    default:
      return initList(-1);
  }
}

public ArrayList<PVector> wallSet( int i ){
  ArrayList<PVector> wall = new ArrayList<PVector>();
  wall.clear();
  switch( i ){
    case 0:  //\u5317\u6d77\u9053
      wall.add( new PVector( width / 1.48f, height / 3.29f ) );
      wall.add( new PVector( width / 1.50f, height / 3.45f ) );
      wall.add( new PVector( width / 1.48f, height / 3.66f ) );
      wall.add( new PVector( width / 1.48f, height / 3.79f ) );
      wall.add( new PVector( width / 1.52f, height / 4.04f ) );
      wall.add( new PVector( width / 1.52f, height / 4.19f ) );
      wall.add( new PVector( width / 1.52f, height / 4.54f ) );
      wall.add( new PVector( width / 1.44f, height / 5.19f ) );
      wall.add( new PVector( width / 1.46f, height / 5.45f ) );
      wall.add( new PVector( width / 1.45f, height / 5.74f ) );
      wall.add( new PVector( width / 1.44f, height / 5.90f ) );
      wall.add( new PVector( width / 1.40f, height / 5.45f ) );
      wall.add( new PVector( width / 1.39f, height / 5.65f ) );
      wall.add( new PVector( width / 1.37f, height / 5.45f ) );
      wall.add( new PVector( width / 1.34f, height / 5.97f ) );
      wall.add( new PVector( width / 1.35f, height / 6.97f ) );
      wall.add( new PVector( width / 1.32f, height / 7.66f ) );
      wall.add( new PVector( width / 1.32f, height / 9.56f ) );
      wall.add( new PVector( width / 1.31f, height / 11.61f ) );
      wall.add( new PVector( width / 1.31f, height / 16.36f ) );
      wall.add( new PVector( width / 1.33f, height / 23.48f ) );
      wall.add( new PVector( width / 1.33f, height / 38.57f ) );
      wall.add( new PVector( width / 1.30f, height / 60.00f ) );
      wall.add( new PVector( width / 1.19f, height / 10.69f ) );
      wall.add( new PVector( width / 1.12f, height / 8.44f ) );
      wall.add( new PVector( width / 1.08f, height / 7.66f ) );
      wall.add( new PVector( width / 1.05f, height / 9.56f ) );
      wall.add( new PVector( width / 1.04f, height / 9.15f ) );
      wall.add( new PVector( width / 1.06f, height / 7.06f ) );
      wall.add( new PVector( width / 1.05f, height / 6.35f ) );
      wall.add( new PVector( width / 1.04f, height / 5.74f ) );
      wall.add( new PVector( width / 1.02f, height / 6.00f ) );
      wall.add( new PVector( width / 1.04f, height / 5.40f ) );
      wall.add( new PVector( width / 1.06f, height / 5.37f ) );
      wall.add( new PVector( width / 1.06f, height / 5.14f ) );
      wall.add( new PVector( width / 1.08f, height / 5.27f ) );
      wall.add( new PVector( width / 1.08f, height / 5.07f ) );
      wall.add( new PVector( width / 1.12f, height / 5.07f ) );
      wall.add( new PVector( width / 1.18f, height / 4.27f ) );
      wall.add( new PVector( width / 1.18f, height / 3.72f ) );
      wall.add( new PVector( width / 1.32f, height / 4.54f ) );
      wall.add( new PVector( width / 1.39f, height / 4.19f ) );
      wall.add( new PVector( width / 1.41f, height / 4.48f ) );
      wall.add( new PVector( width / 1.45f, height / 4.44f ) );
      wall.add( new PVector( width / 1.47f, height / 4.11f ) );
      wall.add( new PVector( width / 1.44f, height / 3.91f ) );
      wall.add( new PVector( width / 1.41f, height / 3.96f ) );
      wall.add( new PVector( width / 1.40f, height / 3.91f ) );
      wall.add( new PVector( width / 1.40f, height / 3.75f ) );
      wall.add( new PVector( width / 1.37f, height / 3.66f ) );
      wall.add( new PVector( width / 1.39f, height / 3.56f ) );
      wall.add( new PVector( width / 1.41f, height / 3.65f ) );
      wall.add( new PVector( width / 1.43f, height / 3.56f ) );
      wall.add( new PVector( width / 1.45f, height / 3.56f ) );
      wall.add( new PVector( width / 1.45f, height / 3.43f ) );
      wall.add( new PVector( width / 1.48f, height / 3.29f ) );
      break;
    case 1:  //\u9752\u68ee
      wall.add( new PVector( width / 1.50f, height / 2.73f ) );
      wall.add( new PVector( width / 1.50f, height / 2.82f ) );
      wall.add( new PVector( width / 1.50f, height / 2.90f ) );
      wall.add( new PVector( width / 1.47f, height / 2.91f ) );
      wall.add( new PVector( width / 1.46f, height / 3.20f ) );
      wall.add( new PVector( width / 1.43f, height / 3.20f ) );
      wall.add( new PVector( width / 1.42f, height / 2.96f ) );
      wall.add( new PVector( width / 1.40f, height / 2.96f ) );
      wall.add( new PVector( width / 1.40f, height / 3.02f ) );
      wall.add( new PVector( width / 1.37f, height / 2.96f ) );
      wall.add( new PVector( width / 1.36f, height / 3.10f ) );
      wall.add( new PVector( width / 1.37f, height / 3.15f ) );
      wall.add( new PVector( width / 1.41f, height / 3.12f ) );
      wall.add( new PVector( width / 1.40f, height / 3.36f ) );
      wall.add( new PVector( width / 1.36f, height / 3.27f ) );
      wall.add( new PVector( width / 1.34f, height / 3.29f ) );
      wall.add( new PVector( width / 1.35f, height / 3.10f ) );
      wall.add( new PVector( width / 1.34f, height / 2.81f ) );
      wall.add( new PVector( width / 1.32f, height / 2.73f ) );
      wall.add( new PVector( width / 1.39f, height / 2.66f ) );
      wall.add( new PVector( width / 1.38f, height / 2.70f ) );
      wall.add( new PVector( width / 1.40f, height / 2.76f ) );
      wall.add( new PVector( width / 1.42f, height / 2.71f ) );
      wall.add( new PVector( width / 1.45f, height / 2.75f ) );
      wall.add( new PVector( width / 1.50f, height / 2.73f ) );
      break;
    case 2:  //\u5ca9\u624b
      wall.add( new PVector( width / 1.39f, height / 2.65f ) );
      wall.add( new PVector( width / 1.32f, height / 2.71f ) );
      wall.add( new PVector( width / 1.30f, height / 2.51f ) );
      wall.add( new PVector( width / 1.29f, height / 2.39f ) );
      wall.add( new PVector( width / 1.28f, height / 2.38f ) );
      wall.add( new PVector( width / 1.28f, height / 2.32f ) );
      wall.add( new PVector( width / 1.29f, height / 2.32f ) );
      wall.add( new PVector( width / 1.29f, height / 2.23f ) );
      wall.add( new PVector( width / 1.32f, height / 2.16f ) );
      wall.add( new PVector( width / 1.33f, height / 2.17f ) );
      wall.add( new PVector( width / 1.340f, height / 2.164f ) );
      wall.add( new PVector( width / 1.37f, height / 2.10f ) );
      wall.add( new PVector( width / 1.37f, height / 2.14f ) );
      wall.add( new PVector( width / 1.41f, height / 2.16f ) );
      wall.add( new PVector( width / 1.42f, height / 2.27f ) );
      wall.add( new PVector( width / 1.41f, height / 2.35f ) );
      wall.add( new PVector( width / 1.41f, height / 2.43f ) );
      wall.add( new PVector( width / 1.40f, height / 2.62f ) );
      wall.add( new PVector( width / 1.39f, height / 2.65f ) );
      break;
    case 3:  //\u5bae\u57ce
      wall.add( new PVector( width / 1.43f, height / 2.12f ) );
      wall.add( new PVector( width / 1.41f, height / 2.15f ) );
      wall.add( new PVector( width / 1.37f, height / 2.14f ) );
      wall.add( new PVector( width / 1.37f, height / 2.11f ) );
      wall.add( new PVector( width / 1.34f, height / 2.16f ) );
      wall.add( new PVector( width / 1.32f, height / 2.15f ) );
      wall.add( new PVector( width / 1.34f, height / 2.07f ) );
      wall.add( new PVector( width / 1.34f, height / 1.98f ) );
      wall.add( new PVector( width / 1.35f, height / 2.00f ) );
      wall.add( new PVector( width / 1.39f, height / 1.95f ) );
      wall.add( new PVector( width / 1.39f, height / 1.89f ) );
      wall.add( new PVector( width / 1.41f, height / 1.86f ) );
      wall.add( new PVector( width / 1.46f, height / 1.90f ) );
      wall.add( new PVector( width / 1.43f, height / 1.99f ) );
      wall.add( new PVector( width / 1.44f, height / 2.07f ) );
      wall.add( new PVector( width / 1.43f, height / 2.07f ) );
      wall.add( new PVector( width / 1.43f, height / 2.12f ) );
      wall.add( new PVector( width / 1.43f, height / 2.12f ) );
      break;
    case 4:  //\u79cb\u7530
      wall.add( new PVector( width / 1.53f, height / 2.51f ) );
      wall.add( new PVector( width / 1.51f, height / 2.56f ) );
      wall.add( new PVector( width / 1.50f, height / 2.70f ) );
      wall.add( new PVector( width / 1.45f, height / 2.73f ) );
      wall.add( new PVector( width / 1.43f, height / 2.70f ) );
      wall.add( new PVector( width / 1.40f, height / 2.74f ) );
      wall.add( new PVector( width / 1.39f, height / 2.72f ) );
      wall.add( new PVector( width / 1.39f, height / 2.68f ) );
      wall.add( new PVector( width / 1.384f, height / 2.693f ) );
      wall.add( new PVector( width / 1.405f, height / 2.628f ) );
      wall.add( new PVector( width / 1.41f, height / 2.43f ) );
      wall.add( new PVector( width / 1.41f, height / 2.38f ) );
      wall.add( new PVector( width / 1.42f, height / 2.30f ) );
      wall.add( new PVector( width / 1.42f, height / 2.22f ) );
      wall.add( new PVector( width / 1.41f, height / 2.18f ) );
      wall.add( new PVector( width / 1.43f, height / 2.12f ) );
      wall.add( new PVector( width / 1.44f, height / 2.15f ) );
      wall.add( new PVector( width / 1.46f, height / 2.17f ) );
      wall.add( new PVector( width / 1.51f, height / 2.20f ) );
      wall.add( new PVector( width / 1.49f, height / 2.42f ) );
      wall.add( new PVector( width / 1.50f, height / 2.47f ) );
      wall.add( new PVector( width / 1.53f, height / 2.46f ) );
      wall.add( new PVector( width / 1.53f, height / 2.51f ) );
      break;
    case 5:  //\u5c71\u5f62
      wall.add( new PVector( width / 1.51f, height / 2.20f ) );
      wall.add( new PVector( width / 1.44f, height / 2.15f ) );
      wall.add( new PVector( width / 1.43f, height / 2.09f ) );
      wall.add( new PVector( width / 1.44f, height / 2.07f ) );
      wall.add( new PVector( width / 1.43f, height / 2.00f ) );
      wall.add( new PVector( width / 1.45f, height / 1.93f ) );
      wall.add( new PVector( width / 1.47f, height / 1.91f ) );
      wall.add( new PVector( width / 1.47f, height / 1.86f ) );
      wall.add( new PVector( width / 1.53f, height / 1.86f ) );
      wall.add( new PVector( width / 1.55f, height / 1.88f ) );
      wall.add( new PVector( width / 1.55f, height / 1.90f ) );
      wall.add( new PVector( width / 1.55f, height / 1.93f ) );
      wall.add( new PVector( width / 1.52f, height / 1.96f ) );
      wall.add( new PVector( width / 1.56f, height / 2.03f ) );
      wall.add( new PVector( width / 1.51f, height / 2.20f ) );
      break;
    case 6:  //\u798f\u5cf6
      wall.add( new PVector( width / 1.39f, height / 1.88f ) );
      wall.add( new PVector( width / 1.38f, height / 1.79f ) );
      wall.add( new PVector( width / 1.39f, height / 1.69f ) );
      wall.add( new PVector( width / 1.41f, height / 1.68f ) );
      wall.add( new PVector( width / 1.44f, height / 1.69f ) );
      wall.add( new PVector( width / 1.44f, height / 1.66f ) );
      wall.add( new PVector( width / 1.47f, height / 1.68f ) );
      wall.add( new PVector( width / 1.50f, height / 1.72f ) );
      wall.add( new PVector( width / 1.56f, height / 1.69f ) );
      wall.add( new PVector( width / 1.60f, height / 1.69f ) );
      wall.add( new PVector( width / 1.60f, height / 1.79f ) );
      wall.add( new PVector( width / 1.55f, height / 1.81f ) );
      wall.add( new PVector( width / 1.55f, height / 1.85f ) );
      wall.add( new PVector( width / 1.52f, height / 1.86f ) );
      wall.add( new PVector( width / 1.47f, height / 1.85f ) );
      wall.add( new PVector( width / 1.47f, height / 1.90f ) );
      wall.add( new PVector( width / 1.44f, height / 1.89f ) );
      wall.add( new PVector( width / 1.41f, height / 1.85f ) );
      wall.add( new PVector( width / 1.39f, height / 1.88f ) );
      break;
    case 7:  //\u8328\u57ce
      wall.add( new PVector( width / 1.47f, height / 1.69f ) );
      wall.add( new PVector( width / 1.44f, height / 1.66f ) );
      wall.add( new PVector( width / 1.44f, height / 1.69f ) );
      wall.add( new PVector( width / 1.41f, height / 1.67f ) );
      wall.add( new PVector( width / 1.43f, height / 1.57f ) );
      wall.add( new PVector( width / 1.42f, height / 1.52f ) );
      wall.add( new PVector( width / 1.44f, height / 1.53f ) );
      wall.add( new PVector( width / 1.47f, height / 1.52f ) );
      wall.add( new PVector( width / 1.51f, height / 1.52f ) );
      wall.add( new PVector( width / 1.54f, height / 1.56f ) );
      wall.add( new PVector( width / 1.51f, height / 1.59f ) );
      wall.add( new PVector( width / 1.47f, height / 1.60f ) );
      wall.add( new PVector( width / 1.47f, height / 1.69f ) );
      break;
    case 8:  //\u6803\u6728
      wall.add( new PVector( width / 1.58f, height / 1.68f ) );
      wall.add( new PVector( width / 1.51f, height / 1.72f ) );
      wall.add( new PVector( width / 1.49f, height / 1.71f ) );
      wall.add( new PVector( width / 1.47f, height / 1.68f ) );
      wall.add( new PVector( width / 1.47f, height / 1.61f ) );
      wall.add( new PVector( width / 1.51f, height / 1.59f ) );
      wall.add( new PVector( width / 1.54f, height / 1.57f ) );
      wall.add( new PVector( width / 1.55f, height / 1.58f ) );
      wall.add( new PVector( width / 1.58f, height / 1.59f ) );
      wall.add( new PVector( width / 1.57f, height / 1.63f ) );
      wall.add( new PVector( width / 1.59f, height / 1.64f ) );
      wall.add( new PVector( width / 1.58f, height / 1.68f ) );
      break;
    case 9:  //\u7fa4\u99ac
      wall.add( new PVector( width / 1.63f, height / 1.70f ) );
      wall.add( new PVector( width / 1.58f, height / 1.69f ) );
      wall.add( new PVector( width / 1.59f, height / 1.64f ) );
      wall.add( new PVector( width / 1.57f, height / 1.63f ) );
      wall.add( new PVector( width / 1.58f, height / 1.59f ) );
      wall.add( new PVector( width / 1.55f, height / 1.58f ) );
      wall.add( new PVector( width / 1.55f, height / 1.57f ) );
      wall.add( new PVector( width / 1.61f, height / 1.57f ) );
      wall.add( new PVector( width / 1.67f, height / 1.54f ) );
      wall.add( new PVector( width / 1.69f, height / 1.56f ) );
      wall.add( new PVector( width / 1.68f, height / 1.59f ) );
      wall.add( new PVector( width / 1.73f, height / 1.61f ) );
      wall.add( new PVector( width / 1.71f, height / 1.64f ) );
      wall.add( new PVector( width / 1.67f, height / 1.65f ) );
      wall.add( new PVector( width / 1.63f, height / 1.70f ) );
      break;
    case 10:  //\u57fc\u7389
      wall.add( new PVector( width / 1.67f, height / 1.52f ) );
      wall.add( new PVector( width / 1.68f, height / 1.54f ) );
      wall.add( new PVector( width / 1.63f, height / 1.56f ) );
      wall.add( new PVector( width / 1.61f, height / 1.58f ) );
      wall.add( new PVector( width / 1.54f, height / 1.56f ) );
      wall.add( new PVector( width / 1.52f, height / 1.54f ) );
      wall.add( new PVector( width / 1.52f, height / 1.50f ) );
      wall.add( new PVector( width / 1.58f, height / 1.50f ) );
      wall.add( new PVector( width / 1.62f, height / 1.52f ) );
      wall.add( new PVector( width / 1.68f, height / 1.52f ) );
      wall.add( new PVector( width / 1.67f, height / 1.52f ) );
      break;
    case 11:  //\u5343\u8449
      wall.add( new PVector( width / 1.51f, height / 1.53f ) );
      wall.add( new PVector( width / 1.47f, height / 1.52f ) );
      wall.add( new PVector( width / 1.44f, height / 1.52f ) );
      wall.add( new PVector( width / 1.40f, height / 1.50f ) );
      wall.add( new PVector( width / 1.44f, height / 1.48f ) );
      wall.add( new PVector( width / 1.45f, height / 1.46f ) );
      wall.add( new PVector( width / 1.45f, height / 1.43f ) );
      wall.add( new PVector( width / 1.49f, height / 1.42f ) );
      wall.add( new PVector( width / 1.51f, height / 1.40f ) );
      wall.add( new PVector( width / 1.51f, height / 1.40f ) );
      wall.add( new PVector( width / 1.52f, height / 1.41f ) );
      wall.add( new PVector( width / 1.52f, height / 1.45f ) );
      wall.add( new PVector( width / 1.49f, height / 1.47f ) );
      wall.add( new PVector( width / 1.50f, height / 1.48f ) );
      wall.add( new PVector( width / 1.51f, height / 1.48f ) );
      wall.add( new PVector( width / 1.51f, height / 1.53f ) );
      break;
    case 12:  //\u6771\u4eac
      wall.add( new PVector( width / 1.64f, height / 1.52f ) );
      wall.add( new PVector( width / 1.58f, height / 1.51f ) );
      wall.add( new PVector( width / 1.515f, height / 1.51f ) );
      wall.add( new PVector( width / 1.515f, height / 1.49f ) );
      wall.add( new PVector( width / 1.53f, height / 1.48f ) );
      wall.add( new PVector( width / 1.56f, height / 1.48f ) );
      wall.add( new PVector( width / 1.57f, height / 1.47f ) );
      wall.add( new PVector( width / 1.61f, height / 1.49f ) );
      wall.add( new PVector( width / 1.64f, height / 1.52f ) );
      break;
    case 13:  //\u795e\u5948\u5ddd
      wall.add( new PVector( width / 1.61f, height / 1.49f ) );
      wall.add( new PVector( width / 1.57f, height / 1.47f ) );
      wall.add( new PVector( width / 1.56f, height / 1.48f ) );
      wall.add( new PVector( width / 1.53f, height / 1.47f ) );
      wall.add( new PVector( width / 1.54f, height / 1.45f ) );
      wall.add( new PVector( width / 1.53f, height / 1.43f ) );
      wall.add( new PVector( width / 1.55f, height / 1.42f ) );
      wall.add( new PVector( width / 1.56f, height / 1.43f ) );
      wall.add( new PVector( width / 1.59f, height / 1.44f ) );
      wall.add( new PVector( width / 1.61f, height / 1.43f ) );
      wall.add( new PVector( width / 1.62f, height / 1.42f ) );
      wall.add( new PVector( width / 1.64f, height / 1.43f ) );
      wall.add( new PVector( width / 1.63f, height / 1.45f ) );
      wall.add( new PVector( width / 1.64f, height / 1.45f ) );
      wall.add( new PVector( width / 1.64f, height / 1.46f ) );
      wall.add( new PVector( width / 1.61f, height / 1.48f ) );
      wall.add( new PVector( width / 1.61f, height / 1.49f ) );
      break;
    case 14:  //\u65b0\u6f5f
      wall.add( new PVector( width / 1.84f, height / 1.69f ) );
      wall.add( new PVector( width / 1.71f, height / 1.77f ) );
      wall.add( new PVector( width / 1.66f, height / 1.86f ) );
      wall.add( new PVector( width / 1.59f, height / 1.91f ) );
      wall.add( new PVector( width / 1.56f, height / 2.03f ) );
      wall.add( new PVector( width / 1.52f, height / 1.98f ) );
      wall.add( new PVector( width / 1.54f, height / 1.95f ) );
      wall.add( new PVector( width / 1.55f, height / 1.89f ) );
      wall.add( new PVector( width / 1.54f, height / 1.86f ) );
      wall.add( new PVector( width / 1.55f, height / 1.83f ) );
      wall.add( new PVector( width / 1.55f, height / 1.80f ) );
      wall.add( new PVector( width / 1.60f, height / 1.78f ) );
      wall.add( new PVector( width / 1.60f, height / 1.78f ) );
      wall.add( new PVector( width / 1.60f, height / 1.69f ) );
      wall.add( new PVector( width / 1.63f, height / 1.70f ) );
      wall.add( new PVector( width / 1.68f, height / 1.65f ) );
      wall.add( new PVector( width / 1.71f, height / 1.69f ) );
      wall.add( new PVector( width / 1.71f, height / 1.69f ) );
      wall.add( new PVector( width / 1.78f, height / 1.67f ) );
      wall.add( new PVector( width / 1.80f, height / 1.68f ) );
      wall.add( new PVector( width / 1.83f, height / 1.66f ) );
      wall.add( new PVector( width / 1.84f, height / 1.69f ) );
      break;
    case 15:  //\u5bcc\u5c71
      wall.add( new PVector( width / 2.019f, height / 1.591f ) );
      wall.add( new PVector( width / 2.019f, height / 1.646f ) );
      wall.add( new PVector( width / 2.002f, height / 1.677f ) );
      wall.add( new PVector( width / 1.965f, height / 1.695f ) );
      wall.add( new PVector( width / 1.975f, height / 1.677f ) );
      wall.add( new PVector( width / 1.937f, height / 1.659f ) );
      wall.add( new PVector( width / 1.903f, height / 1.669f ) );
      wall.add( new PVector( width / 1.848f, height / 1.698f ) );
      wall.add( new PVector( width / 1.836f, height / 1.639f ) );
      wall.add( new PVector( width / 1.860f, height / 1.595f ) );
      wall.add( new PVector( width / 1.912f, height / 1.602f ) );
      wall.add( new PVector( width / 1.945f, height / 1.602f ) );
      wall.add( new PVector( width / 1.981f, height / 1.579f ) );
      wall.add( new PVector( width / 2.002f, height / 1.591f ) );
      wall.add( new PVector( width / 2.019f, height / 1.591f ) );
      break;
    case 16:  //\u77f3\u5ddd
      wall.add( new PVector( width / 2.148f, height / 1.586f ) );
      wall.add( new PVector( width / 2.078f, height / 1.622f ) );
      wall.add( new PVector( width / 2.034f, height / 1.690f ) );
      wall.add( new PVector( width / 2.049f, height / 1.731f ) );
      wall.add( new PVector( width / 2.034f, height / 1.765f ) );
      wall.add( new PVector( width / 1.932f, height / 1.803f ) );
      wall.add( new PVector( width / 1.907f, height / 1.788f ) );
      wall.add( new PVector( width / 1.932f, height / 1.759f ) );
      wall.add( new PVector( width / 1.975f, height / 1.736f ) );
      wall.add( new PVector( width / 1.967f, height / 1.698f ) );
      wall.add( new PVector( width / 2.013f, height / 1.677f ) );
      wall.add( new PVector( width / 2.023f, height / 1.639f ) );
      wall.add( new PVector( width / 2.023f, height / 1.584f ) );
      wall.add( new PVector( width / 2.038f, height / 1.552f ) );
      wall.add( new PVector( width / 2.101f, height / 1.563f ) );
      wall.add( new PVector( width / 2.148f, height / 1.586f ) );
      break;
    case 17:  //\u798f\u4e95
      wall.add( new PVector( width / 2.152f, height / 1.579f ) );
      wall.add( new PVector( width / 2.136f, height / 1.567f ) );
      wall.add( new PVector( width / 2.045f, height / 1.547f ) );
      wall.add( new PVector( width / 2.051f, height / 1.536f ) );
      wall.add( new PVector( width / 2.030f, height / 1.515f ) );
      wall.add( new PVector( width / 2.067f, height / 1.508f ) );
      wall.add( new PVector( width / 2.129f, height / 1.504f ) );
      wall.add( new PVector( width / 2.140f, height / 1.490f ) );
      wall.add( new PVector( width / 2.177f, height / 1.492f ) );
      wall.add( new PVector( width / 2.189f, height / 1.475f ) );
      wall.add( new PVector( width / 2.240f, height / 1.465f ) );
      wall.add( new PVector( width / 2.288f, height / 1.448f ) );
      wall.add( new PVector( width / 2.350f, height / 1.448f ) );
      wall.add( new PVector( width / 2.373f, height / 1.471f ) );
      wall.add( new PVector( width / 2.302f, height / 1.467f ) );
      wall.add( new PVector( width / 2.270f, height / 1.481f ) );
      wall.add( new PVector( width / 2.235f, height / 1.481f ) );
      wall.add( new PVector( width / 2.197f, height / 1.506f ) );
      wall.add( new PVector( width / 2.222f, height / 1.534f ) );
      wall.add( new PVector( width / 2.204f, height / 1.556f ) );
      wall.add( new PVector( width / 2.152f, height / 1.579f ) );
      break;
    case 18:  //\u5c71\u68a8
      wall.add( new PVector( width / 1.727f, height / 1.528f ) );
      wall.add( new PVector( width / 1.686f, height / 1.517f ) );
      wall.add( new PVector( width / 1.641f, height / 1.510f ) );
      wall.add( new PVector( width / 1.638f, height / 1.513f ) );
      wall.add( new PVector( width / 1.615f, height / 1.492f ) );
      wall.add( new PVector( width / 1.615f, height / 1.481f ) );
      wall.add( new PVector( width / 1.641f, height / 1.452f ) );
      wall.add( new PVector( width / 1.671f, height / 1.450f ) );
      wall.add( new PVector( width / 1.701f, height / 1.452f ) );
      wall.add( new PVector( width / 1.711f, height / 1.427f ) );
      wall.add( new PVector( width / 1.745f, height / 1.446f ) );
      wall.add( new PVector( width / 1.755f, height / 1.490f ) );
      wall.add( new PVector( width / 1.750f, height / 1.517f ) );
      wall.add( new PVector( width / 1.727f, height / 1.528f ) );
      break;
    case 19:  //\u9577\u91ce
      wall.add( new PVector( width / 1.912f, height / 1.517f ) );
      wall.add( new PVector( width / 1.870f, height / 1.528f ) );
      wall.add( new PVector( width / 1.870f, height / 1.549f ) );
      wall.add( new PVector( width / 1.851f, height / 1.574f ) );
      wall.add( new PVector( width / 1.857f, height / 1.591f ) );
      wall.add( new PVector( width / 1.830f, height / 1.644f ) );
      wall.add( new PVector( width / 1.805f, height / 1.677f ) );
      wall.add( new PVector( width / 1.776f, height / 1.669f ) );
      wall.add( new PVector( width / 1.711f, height / 1.690f ) );
      wall.add( new PVector( width / 1.686f, height / 1.651f ) );
      wall.add( new PVector( width / 1.719f, height / 1.639f ) );
      wall.add( new PVector( width / 1.731f, height / 1.610f ) );
      wall.add( new PVector( width / 1.686f, height / 1.591f ) );
      wall.add( new PVector( width / 1.693f, height / 1.556f ) );
      wall.add( new PVector( width / 1.678f, height / 1.523f ) );
      wall.add( new PVector( width / 1.727f, height / 1.528f ) );
      wall.add( new PVector( width / 1.755f, height / 1.519f ) );
      wall.add( new PVector( width / 1.758f, height / 1.488f ) );
      wall.add( new PVector( width / 1.771f, height / 1.452f ) );
      wall.add( new PVector( width / 1.830f, height / 1.429f ) );
      wall.add( new PVector( width / 1.870f, height / 1.432f ) );

      wall.add( new PVector( width / 1.866f, height / 1.467f ) );
      wall.add( new PVector( width / 1.912f, height / 1.517f ) );
      break;
    case 20:  //\u5c90\u961c
      wall.add( new PVector( width / 2.047f, height / 1.541f ) );
      wall.add( new PVector( width / 2.021f, height / 1.572f ) );
      wall.add( new PVector( width / 2.021f, height / 1.584f ) );
      wall.add( new PVector( width / 2.006f, height / 1.586f ) );
      wall.add( new PVector( width / 1.986f, height / 1.579f ) );
      wall.add( new PVector( width / 1.953f, height / 1.595f ) );
      wall.add( new PVector( width / 1.914f, height / 1.598f ) );
      wall.add( new PVector( width / 1.864f, height / 1.591f ) );
      wall.add( new PVector( width / 1.860f, height / 1.579f ) );
      wall.add( new PVector( width / 1.873f, height / 1.552f ) );
      wall.add( new PVector( width / 1.873f, height / 1.534f ) );
      wall.add( new PVector( width / 1.916f, height / 1.517f ) );
      wall.add( new PVector( width / 1.870f, height / 1.467f ) );
      wall.add( new PVector( width / 1.877f, height / 1.438f ) );
      wall.add( new PVector( width / 1.914f, height / 1.432f ) );
      wall.add( new PVector( width / 2.000f, height / 1.452f ) );
      wall.add( new PVector( width / 2.043f, height / 1.448f ) );
      wall.add( new PVector( width / 2.053f, height / 1.429f ) );
      wall.add( new PVector( width / 2.089f, height / 1.432f ) );
      wall.add( new PVector( width / 2.119f, height / 1.432f ) );
      wall.add( new PVector( width / 2.119f, height / 1.457f ) );
      wall.add( new PVector( width / 2.140f, height / 1.481f ) );
      wall.add( new PVector( width / 2.124f, height / 1.506f ) );
      wall.add( new PVector( width / 2.067f, height / 1.502f ) );
      wall.add( new PVector( width / 2.023f, height / 1.517f ) );
      wall.add( new PVector( width / 2.047f, height / 1.541f ) );
      break;
    case 21:  //\u9759\u5ca1
      wall.add( new PVector( width / 1.825f, height / 1.429f ) );
      wall.add( new PVector( width / 1.771f, height / 1.446f ) );
      wall.add( new PVector( width / 1.760f, height / 1.477f ) );
      wall.add( new PVector( width / 1.750f, height / 1.448f ) );
      wall.add( new PVector( width / 1.724f, height / 1.429f ) );
      wall.add( new PVector( width / 1.705f, height / 1.432f ) );
      wall.add( new PVector( width / 1.696f, height / 1.448f ) );
      wall.add( new PVector( width / 1.635f, height / 1.448f ) );
      wall.add( new PVector( width / 1.640f, height / 1.432f ) );
      wall.add( new PVector( width / 1.622f, height / 1.417f ) );
      wall.add( new PVector( width / 1.615f, height / 1.395f ) );
      wall.add( new PVector( width / 1.657f, height / 1.360f ) );
      wall.add( new PVector( width / 1.671f, height / 1.369f ) );
      wall.add( new PVector( width / 1.671f, height / 1.404f ) );
      wall.add( new PVector( width / 1.649f, height / 1.410f ) );
      wall.add( new PVector( width / 1.678f, height / 1.423f ) );
      wall.add( new PVector( width / 1.734f, height / 1.386f ) );
      wall.add( new PVector( width / 1.760f, height / 1.369f ) );
      wall.add( new PVector( width / 1.755f, height / 1.355f ) );
      wall.add( new PVector( width / 1.822f, height / 1.360f ) );
      wall.add( new PVector( width / 1.884f, height / 1.364f ) );
      wall.add( new PVector( width / 1.881f, height / 1.383f ) );
      wall.add( new PVector( width / 1.825f, height / 1.429f ) );
      break;
    case 22:  //\u611b\u77e5
      wall.add( new PVector( width / 2.053f, height / 1.423f ) );
      wall.add( new PVector( width / 2.043f, height / 1.446f ) );
      wall.add( new PVector( width / 2.004f, height / 1.450f ) );
      wall.add( new PVector( width / 1.907f, height / 1.432f ) );
      wall.add( new PVector( width / 1.879f, height / 1.436f ) );
      wall.add( new PVector( width / 1.864f, height / 1.427f ) );
      wall.add( new PVector( width / 1.835f, height / 1.427f ) );
      wall.add( new PVector( width / 1.855f, height / 1.399f ) );
      wall.add( new PVector( width / 1.888f, height / 1.369f ) );
      wall.add( new PVector( width / 1.975f, height / 1.352f ) );
      wall.add( new PVector( width / 1.979f, height / 1.358f ) );
      wall.add( new PVector( width / 1.920f, height / 1.372f ) );
      wall.add( new PVector( width / 1.930f, height / 1.378f ) );
      wall.add( new PVector( width / 1.965f, height / 1.378f ) );
      wall.add( new PVector( width / 1.990f, height / 1.385f ) );
      wall.add( new PVector( width / 1.990f, height / 1.369f ) );
      wall.add( new PVector( width / 2.010f, height / 1.376f ) );
      wall.add( new PVector( width / 2.010f, height / 1.408f ) );
      wall.add( new PVector( width / 2.032f, height / 1.408f ) );
      wall.add( new PVector( width / 2.053f, height / 1.423f ) );
      break;
    case 23:  //\u4e09\u91cd
      wall.add( new PVector( width / 2.199f, height / 1.378f ) );
      wall.add( new PVector( width / 2.169f, height / 1.390f ) );
      wall.add( new PVector( width / 2.122f, height / 1.394f ) );
      wall.add( new PVector( width / 2.110f, height / 1.399f ) );
      wall.add( new PVector( width / 2.115f, height / 1.427f ) );
      wall.add( new PVector( width / 2.069f, height / 1.427f ) );
      wall.add( new PVector( width / 2.043f, height / 1.408f ) );
      wall.add( new PVector( width / 2.087f, height / 1.372f ) );
      wall.add( new PVector( width / 2.000f, height / 1.342f ) );
      wall.add( new PVector( width / 2.010f, height / 1.322f ) );
      wall.add( new PVector( width / 2.080f, height / 1.322f ) );
      wall.add( new PVector( width / 2.145f, height / 1.311f ) );
      wall.add( new PVector( width / 2.145f, height / 1.295f ) );
      wall.add( new PVector( width / 2.207f, height / 1.275f ) );
      wall.add( new PVector( width / 2.215f, height / 1.260f ) );
      wall.add( new PVector( width / 2.259f, height / 1.278f ) );
      wall.add( new PVector( width / 2.199f, height / 1.301f ) );
      wall.add( new PVector( width / 2.194f, height / 1.330f ) );
      wall.add( new PVector( width / 2.157f, height / 1.350f ) );
      wall.add( new PVector( width / 2.199f, height / 1.350f ) );
      wall.add( new PVector( width / 2.199f, height / 1.350f ) );
      wall.add( new PVector( width / 2.202f, height / 1.376f ) );
      wall.add( new PVector( width / 2.199f, height / 1.378f ) );
      break;
    case 24:  //\u6ecb\u8cc0
      wall.add( new PVector( width / 2.278f, height / 1.446f ) );
      wall.add( new PVector( width / 2.246f, height / 1.459f ) );
      wall.add( new PVector( width / 2.187f, height / 1.469f ) );
      wall.add( new PVector( width / 2.174f, height / 1.486f ) );
      wall.add( new PVector( width / 2.145f, height / 1.486f ) );
      wall.add( new PVector( width / 2.145f, height / 1.475f ) );
      wall.add( new PVector( width / 2.129f, height / 1.456f ) );
      wall.add( new PVector( width / 2.129f, height / 1.427f ) );
      wall.add( new PVector( width / 2.117f, height / 1.399f ) );
      wall.add( new PVector( width / 2.177f, height / 1.390f ) );
      wall.add( new PVector( width / 2.207f, height / 1.381f ) );
      wall.add( new PVector( width / 2.264f, height / 1.403f ) );
      wall.add( new PVector( width / 2.278f, height / 1.446f ) );
      break;
    case 25:  //\u4eac\u90fd
      wall.add( new PVector( width / 2.563f, height / 1.486f ) );
      wall.add( new PVector( width / 2.520f, height / 1.490f ) );
      wall.add( new PVector( width / 2.449f, height / 1.502f ) );
      wall.add( new PVector( width / 2.427f, height / 1.502f ) );
      wall.add( new PVector( width / 2.427f, height / 1.481f ) );
      wall.add( new PVector( width / 2.440f, height / 1.475f ) );
      wall.add( new PVector( width / 2.418f, height / 1.471f ) );
      wall.add( new PVector( width / 2.382f, height / 1.471f ) );
      wall.add( new PVector( width / 2.359f, height / 1.446f ) );
      wall.add( new PVector( width / 2.283f, height / 1.442f ) );
      wall.add( new PVector( width / 2.275f, height / 1.408f ) );
      wall.add( new PVector( width / 2.215f, height / 1.378f ) );
      wall.add( new PVector( width / 2.288f, height / 1.376f ) );
      wall.add( new PVector( width / 2.316f, height / 1.399f ) );
      wall.add( new PVector( width / 2.344f, height / 1.395f ) );
      wall.add( new PVector( width / 2.388f, height / 1.408f ) );
      wall.add( new PVector( width / 2.403f, height / 1.421f ) );
      wall.add( new PVector( width / 2.546f, height / 1.450f ) );
      wall.add( new PVector( width / 2.507f, height / 1.465f ) );
      wall.add( new PVector( width / 2.563f, height / 1.475f ) );
      wall.add( new PVector( width / 2.563f, height / 1.486f ) );
      break;
    case 26:  //\u5927\u962a
      wall.add( new PVector( width / 2.388f, height / 1.404f ) );
      wall.add( new PVector( width / 2.310f, height / 1.392f ) );
      wall.add( new PVector( width / 2.302f, height / 1.378f ) );
      wall.add( new PVector( width / 2.308f, height / 1.352f ) );
      wall.add( new PVector( width / 2.316f, height / 1.335f ) );
      wall.add( new PVector( width / 2.481f, height / 1.327f ) );
      wall.add( new PVector( width / 2.412f, height / 1.343f ) );
      wall.add( new PVector( width / 2.379f, height / 1.378f ) );
      wall.add( new PVector( width / 2.388f, height / 1.404f ) );
      break;
    case 27:  //\u5175\u5eab
      wall.add( new PVector( width / 2.743f, height / 1.479f ) );
      wall.add( new PVector( width / 2.697f, height / 1.486f ) );
      wall.add( new PVector( width / 2.570f, height / 1.486f ) );
      wall.add( new PVector( width / 2.577f, height / 1.475f ) );
      wall.add( new PVector( width / 2.513f, height / 1.465f ) );
      wall.add( new PVector( width / 2.560f, height / 1.450f ) );
      wall.add( new PVector( width / 2.409f, height / 1.417f ) );
      wall.add( new PVector( width / 2.409f, height / 1.395f ) );
      wall.add( new PVector( width / 2.388f, height / 1.386f ) );
      wall.add( new PVector( width / 2.394f, height / 1.369f ) );
      wall.add( new PVector( width / 2.440f, height / 1.369f ) );
      wall.add( new PVector( width / 2.494f, height / 1.360f ) );
      wall.add( new PVector( width / 2.595f, height / 1.376f ) );
      wall.add( new PVector( width / 2.697f, height / 1.376f ) );
      wall.add( new PVector( width / 2.775f, height / 1.372f ) );
      wall.add( new PVector( width / 2.803f, height / 1.390f ) );
      wall.add( new PVector( width / 2.755f, height / 1.430f ) );
      wall.add( new PVector( width / 2.697f, height / 1.442f ) );
      wall.add( new PVector( width / 2.743f, height / 1.479f ) );
      break;
    case 28:  //\u5948\u826f
      wall.add( new PVector( width / 2.294f, height / 1.372f ) );
      wall.add( new PVector( width / 2.207f, height / 1.374f ) );
      wall.add( new PVector( width / 2.207f, height / 1.352f ) );
      wall.add( new PVector( width / 2.169f, height / 1.343f ) );
      wall.add( new PVector( width / 2.202f, height / 1.332f ) );
      wall.add( new PVector( width / 2.202f, height / 1.303f ) );
      wall.add( new PVector( width / 2.264f, height / 1.283f ) );
      wall.add( new PVector( width / 2.319f, height / 1.283f ) );
      wall.add( new PVector( width / 2.341f, height / 1.303f ) );
      wall.add( new PVector( width / 2.299f, height / 1.319f ) );
      wall.add( new PVector( width / 2.313f, height / 1.335f ) );
      wall.add( new PVector( width / 2.294f, height / 1.372f ) );
      break;
    case 29:  //\u548c\u6b4c\u5c71
      wall.add( new PVector( width / 2.494f, height / 1.322f ) );
      wall.add( new PVector( width / 2.316f, height / 1.330f ) );
      wall.add( new PVector( width / 2.308f, height / 1.319f ) );
      wall.add( new PVector( width / 2.344f, height / 1.303f ) );
      wall.add( new PVector( width / 2.327f, height / 1.280f ) );
      wall.add( new PVector( width / 2.267f, height / 1.280f ) );
      wall.add( new PVector( width / 2.222f, height / 1.257f ) );
      wall.add( new PVector( width / 2.280f, height / 1.239f ) );
      wall.add( new PVector( width / 2.400f, height / 1.253f ) );
      wall.add( new PVector( width / 2.400f, height / 1.268f ) );
      wall.add( new PVector( width / 2.497f, height / 1.286f ) );
      wall.add( new PVector( width / 2.471f, height / 1.293f ) );
      wall.add( new PVector( width / 2.494f, height / 1.303f ) );
      wall.add( new PVector( width / 2.455f, height / 1.311f ) );
      wall.add( new PVector( width / 2.494f, height / 1.322f ) );
      break;
    case 30:  //\u9ce5\u53d6
      wall.add( new PVector( width / 3.243f, height / 1.461f ) );
      wall.add( new PVector( width / 3.189f, height / 1.461f ) );
      wall.add( new PVector( width / 3.122f, height / 1.471f ) );
      wall.add( new PVector( width / 2.896f, height / 1.467f ) );
      wall.add( new PVector( width / 2.755f, height / 1.477f ) );
      wall.add( new PVector( width / 2.704f, height / 1.442f ) );
      wall.add( new PVector( width / 2.763f, height / 1.432f ) );
      wall.add( new PVector( width / 2.866f, height / 1.432f ) );
      wall.add( new PVector( width / 2.931f, height / 1.442f ) );
      wall.add( new PVector( width / 3.000f, height / 1.438f ) );
      wall.add( new PVector( width / 3.097f, height / 1.448f ) );
      wall.add( new PVector( width / 3.293f, height / 1.414f ) );
      wall.add( new PVector( width / 3.339f, height / 1.414f ) );
      wall.add( new PVector( width / 3.351f, height / 1.427f ) );
      wall.add( new PVector( width / 3.265f, height / 1.442f ) );
      wall.add( new PVector( width / 3.243f, height / 1.461f ) );
      break;
    case 31:  //\u5cf6\u6839
      wall.add( new PVector( width / 4.571f, height / 1.362f ) );
      wall.add( new PVector( width / 3.699f, height / 1.440f ) );
      wall.add( new PVector( width / 3.714f, height / 1.454f ) );
      wall.add( new PVector( width / 3.404f, height / 1.475f ) );
      wall.add( new PVector( width / 3.328f, height / 1.479f ) );
      wall.add( new PVector( width / 3.260f, height / 1.465f ) );
      wall.add( new PVector( width / 3.282f, height / 1.446f ) );
      wall.add( new PVector( width / 3.368f, height / 1.430f ) );
      wall.add( new PVector( width / 3.368f, height / 1.417f ) );
      wall.add( new PVector( width / 3.556f, height / 1.415f ) );
      wall.add( new PVector( width / 3.678f, height / 1.394f ) );
      wall.add( new PVector( width / 4.111f, height / 1.372f ) );
      wall.add( new PVector( width / 4.220f, height / 1.345f ) );
      wall.add( new PVector( width / 4.374f, height / 1.330f ) );
      wall.add( new PVector( width / 4.571f, height / 1.342f ) );
      wall.add( new PVector( width / 4.571f, height / 1.362f ) );
      break;
    case 32:  //\u5ca1\u5c71
      wall.add( new PVector( width / 3.282f, height / 1.408f ) );
      wall.add( new PVector( width / 3.087f, height / 1.446f ) );
      wall.add( new PVector( width / 3.000f, height / 1.436f ) );
      wall.add( new PVector( width / 2.931f, height / 1.440f ) );
      wall.add( new PVector( width / 2.866f, height / 1.430f ) );
      wall.add( new PVector( width / 2.763f, height / 1.430f ) );
      wall.add( new PVector( width / 2.803f, height / 1.395f ) );
      wall.add( new PVector( width / 2.791f, height / 1.372f ) );
      wall.add( new PVector( width / 2.844f, height / 1.369f ) );
      wall.add( new PVector( width / 2.857f, height / 1.355f ) );
      wall.add( new PVector( width / 2.954f, height / 1.343f ) );
      wall.add( new PVector( width / 3.048f, height / 1.343f ) );
      wall.add( new PVector( width / 3.097f, height / 1.347f ) );
      wall.add( new PVector( width / 3.174f, height / 1.342f ) );
      wall.add( new PVector( width / 3.254f, height / 1.372f ) );
      wall.add( new PVector( width / 3.282f, height / 1.390f ) );
      wall.add( new PVector( width / 3.282f, height / 1.408f ) );
      break;
    case 33:  //\u5e83\u5cf6
      wall.add( new PVector( width / 4.201f, height / 1.342f ) );
      wall.add( new PVector( width / 4.085f, height / 1.372f ) );
      wall.add( new PVector( width / 3.692f, height / 1.388f ) );
      wall.add( new PVector( width / 3.542f, height / 1.412f ) );
      wall.add( new PVector( width / 3.310f, height / 1.412f ) );
      wall.add( new PVector( width / 3.282f, height / 1.385f ) );
      wall.add( new PVector( width / 3.189f, height / 1.345f ) );
      wall.add( new PVector( width / 3.254f, height / 1.333f ) );
      wall.add( new PVector( width / 3.374f, height / 1.337f ) );
      wall.add( new PVector( width / 3.435f, height / 1.328f ) );
      wall.add( new PVector( width / 3.556f, height / 1.328f ) );
      wall.add( new PVector( width / 3.664f, height / 1.317f ) );
      wall.add( new PVector( width / 3.787f, height / 1.317f ) );
      wall.add( new PVector( width / 3.787f, height / 1.328f ) );
      wall.add( new PVector( width / 3.840f, height / 1.333f ) );
      wall.add( new PVector( width / 3.943f, height / 1.330f ) );
      wall.add( new PVector( width / 4.008f, height / 1.317f ) );
      wall.add( new PVector( width / 4.111f, height / 1.328f ) );
      wall.add( new PVector( width / 4.201f, height / 1.342f ) );
      break;
    case 34:  //\u5c71\u53e3
      wall.add( new PVector( width / 5.731f, height / 1.325f ) );
      wall.add( new PVector( width / 5.424f, height / 1.337f ) );
      wall.add( new PVector( width / 5.161f, height / 1.333f ) );
      wall.add( new PVector( width / 4.923f, height / 1.342f ) );
      wall.add( new PVector( width / 4.694f, height / 1.362f ) );
      wall.add( new PVector( width / 4.604f, height / 1.362f ) );
      wall.add( new PVector( width / 4.604f, height / 1.342f ) );
      wall.add( new PVector( width / 4.394f, height / 1.325f ) );
      wall.add( new PVector( width / 4.201f, height / 1.333f ) );
      wall.add( new PVector( width / 4.138f, height / 1.325f ) );
      wall.add( new PVector( width / 4.025f, height / 1.314f ) );
      wall.add( new PVector( width / 4.051f, height / 1.297f ) );
      wall.add( new PVector( width / 4.129f, height / 1.278f ) );
      wall.add( new PVector( width / 4.344f, height / 1.289f ) );
      wall.add( new PVector( width / 4.550f, height / 1.301f ) );
      wall.add( new PVector( width / 4.741f, height / 1.293f ) );
      wall.add( new PVector( width / 4.923f, height / 1.293f ) );
      wall.add( new PVector( width / 5.120f, height / 1.286f ) );
      wall.add( new PVector( width / 5.455f, height / 1.297f ) );
      wall.add( new PVector( width / 5.614f, height / 1.290f ) );
      wall.add( new PVector( width / 5.749f, height / 1.301f ) );
      wall.add( new PVector( width / 5.614f, height / 1.317f ) );
      wall.add( new PVector( width / 5.731f, height / 1.325f ) );
      break;
    case 35:  //\u5fb3\u5cf6
      wall.add( new PVector( width / 3.062f, height / 1.283f ) );
      wall.add( new PVector( width / 3.024f, height / 1.301f ) );
      wall.add( new PVector( width / 2.844f, height / 1.309f ) );
      wall.add( new PVector( width / 2.743f, height / 1.309f ) );
      wall.add( new PVector( width / 2.743f, height / 1.309f ) );
      wall.add( new PVector( width / 2.716f, height / 1.314f ) );
      wall.add( new PVector( width / 2.659f, height / 1.314f ) );
      wall.add( new PVector( width / 2.667f, height / 1.295f ) );
      wall.add( new PVector( width / 2.634f, height / 1.275f ) );
      wall.add( new PVector( width / 2.803f, height / 1.246f ) );
      wall.add( new PVector( width / 2.900f, height / 1.271f ) );
      wall.add( new PVector( width / 2.981f, height / 1.272f ) );
      wall.add( new PVector( width / 3.062f, height / 1.283f ) );
      break;
    case 36:  //\u9999\u5ddd
      wall.add( new PVector( width / 3.112f, height / 1.317f ) );
      wall.add( new PVector( width / 3.038f, height / 1.317f ) );
      wall.add( new PVector( width / 2.968f, height / 1.330f ) );
      wall.add( new PVector( width / 2.914f, height / 1.330f ) );
      wall.add( new PVector( width / 2.857f, height / 1.335f ) );
      wall.add( new PVector( width / 2.815f, height / 1.322f ) );
      wall.add( new PVector( width / 2.735f, height / 1.314f ) );
      wall.add( new PVector( width / 2.767f, height / 1.311f ) );
      wall.add( new PVector( width / 2.836f, height / 1.311f ) );
      wall.add( new PVector( width / 2.900f, height / 1.306f ) );
      wall.add( new PVector( width / 3.028f, height / 1.303f ) );
      wall.add( new PVector( width / 3.062f, height / 1.290f ) );
      wall.add( new PVector( width / 3.102f, height / 1.295f ) );
      wall.add( new PVector( width / 3.087f, height / 1.306f ) );
      wall.add( new PVector( width / 3.112f, height / 1.317f ) );
      break;
    case 37:  //\u611b\u5a9b
      wall.add( new PVector( width / 4.229f, height / 1.233f ) );
      wall.add( new PVector( width / 3.678f, height / 1.262f ) );
      wall.add( new PVector( width / 3.664f, height / 1.283f ) );
      wall.add( new PVector( width / 3.485f, height / 1.306f ) );
      wall.add( new PVector( width / 3.392f, height / 1.287f ) );
      wall.add( new PVector( width / 3.127f, height / 1.295f ) );
      wall.add( new PVector( width / 3.067f, height / 1.290f ) );
      wall.add( new PVector( width / 3.087f, height / 1.284f ) );
      wall.add( new PVector( width / 3.333f, height / 1.272f ) );
      wall.add( new PVector( width / 3.416f, height / 1.260f ) );
      wall.add( new PVector( width / 3.435f, height / 1.246f ) );
      wall.add( new PVector( width / 3.575f, height / 1.243f ) );
      wall.add( new PVector( width / 3.549f, height / 1.233f ) );
      wall.add( new PVector( width / 3.664f, height / 1.219f ) );
      wall.add( new PVector( width / 3.714f, height / 1.192f ) );
      wall.add( new PVector( width / 3.848f, height / 1.195f ) );
      wall.add( new PVector( width / 3.832f, height / 1.222f ) );
      wall.add( new PVector( width / 3.902f, height / 1.229f ) );
      wall.add( new PVector( width / 3.926f, height / 1.239f ) );
      wall.add( new PVector( width / 4.229f, height / 1.233f ) );
      break;
    case 38:  //\u9ad8\u77e5
      wall.add( new PVector( width / 3.699f, height / 1.193f ) );
      wall.add( new PVector( width / 3.643f, height / 1.215f ) );
      wall.add( new PVector( width / 3.529f, height / 1.231f ) );
      wall.add( new PVector( width / 3.542f, height / 1.239f ) );
      wall.add( new PVector( width / 3.429f, height / 1.243f ) );
      wall.add( new PVector( width / 3.386f, height / 1.260f ) );
      wall.add( new PVector( width / 3.316f, height / 1.271f ) );
      wall.add( new PVector( width / 3.077f, height / 1.280f ) );
      wall.add( new PVector( width / 3.005f, height / 1.272f ) );
      wall.add( new PVector( width / 2.900f, height / 1.271f ) );
      wall.add( new PVector( width / 2.815f, height / 1.246f ) );
      wall.add( new PVector( width / 2.836f, height / 1.224f ) );
      wall.add( new PVector( width / 2.857f, height / 1.224f ) );
      wall.add( new PVector( width / 2.977f, height / 1.246f ) );
      wall.add( new PVector( width / 3.072f, height / 1.249f ) );
      wall.add( new PVector( width / 3.189f, height / 1.239f ) );
      wall.add( new PVector( width / 3.316f, height / 1.229f ) );
      wall.add( new PVector( width / 3.316f, height / 1.218f ) );
      wall.add( new PVector( width / 3.459f, height / 1.200f ) );
      wall.add( new PVector( width / 3.459f, height / 1.178f ) );
      wall.add( new PVector( width / 3.529f, height / 1.182f ) );
      wall.add( new PVector( width / 3.596f, height / 1.178f ) );
      wall.add( new PVector( width / 3.714f, height / 1.180f ) );
      wall.add( new PVector( width / 3.699f, height / 1.193f ) );
      break;
    case 39:  //\u798f\u5ca1
      wall.add( new PVector( width / 7.559f, height / 1.246f ) );
      wall.add( new PVector( width / 7.191f, height / 1.256f ) );
      wall.add( new PVector( width / 6.931f, height / 1.251f ) );
      wall.add( new PVector( width / 6.531f, height / 1.271f ) );
      wall.add( new PVector( width / 6.421f, height / 1.278f ) );
      wall.add( new PVector( width / 6.057f, height / 1.286f ) );
      wall.add( new PVector( width / 5.697f, height / 1.283f ) );
      wall.add( new PVector( width / 5.614f, height / 1.286f ) );
      wall.add( new PVector( width / 5.455f, height / 1.281f ) );
      wall.add( new PVector( width / 5.614f, height / 1.275f ) );
      wall.add( new PVector( width / 5.304f, height / 1.253f ) );
      wall.add( new PVector( width / 5.304f, height / 1.246f ) );
      wall.add( new PVector( width / 5.647f, height / 1.241f ) );
      wall.add( new PVector( width / 5.818f, height / 1.234f ) );
      wall.add( new PVector( width / 5.783f, height / 1.213f ) );
      wall.add( new PVector( width / 5.783f, height / 1.207f ) );
      wall.add( new PVector( width / 6.057f, height / 1.213f ) );
      wall.add( new PVector( width / 6.358f, height / 1.207f ) );
      wall.add( new PVector( width / 6.508f, height / 1.197f ) );
      wall.add( new PVector( width / 6.690f, height / 1.197f ) );
      wall.add( new PVector( width / 6.690f, height / 1.207f ) );
      wall.add( new PVector( width / 6.857f, height / 1.213f ) );
      wall.add( new PVector( width / 6.358f, height / 1.230f ) );
      wall.add( new PVector( width / 6.358f, height / 1.237f ) );
      wall.add( new PVector( width / 6.690f, height / 1.234f ) );
      wall.add( new PVector( width / 7.471f, height / 1.241f ) );
      wall.add( new PVector( width / 7.559f, height / 1.246f ) );
      break;
    case 40:  //\u4f50\u8cc0
      wall.add( new PVector( width / 8.571f, height / 1.227f ) );
      wall.add( new PVector( width / 8.205f, height / 1.227f ) );
      wall.add( new PVector( width / 8.533f, height / 1.239f ) );
      wall.add( new PVector( width / 8.348f, height / 1.249f ) );
      wall.add( new PVector( width / 8.033f, height / 1.249f ) );
      wall.add( new PVector( width / 8.033f, height / 1.241f ) );
      wall.add( new PVector( width / 7.619f, height / 1.241f ) );
      wall.add( new PVector( width / 6.737f, height / 1.234f ) );
      wall.add( new PVector( width / 6.421f, height / 1.237f ) );
      wall.add( new PVector( width / 6.421f, height / 1.231f ) );
      wall.add( new PVector( width / 6.931f, height / 1.213f ) );
      wall.add( new PVector( width / 7.191f, height / 1.218f ) );
      wall.add( new PVector( width / 7.471f, height / 1.211f ) );
      wall.add( new PVector( width / 7.191f, height / 1.197f ) );
      wall.add( new PVector( width / 7.529f, height / 1.197f ) );
      wall.add( new PVector( width / 8.276f, height / 1.216f ) );
      wall.add( new PVector( width / 8.571f, height / 1.227f ) );
      break;
    case 41:  //\u9577\u5d0e
      wall.add( new PVector( width / 9.505f, height / 1.231f ) );
      wall.add( new PVector( width / 8.767f, height / 1.231f ) );
      wall.add( new PVector( width / 8.727f, height / 1.222f ) );
      wall.add( new PVector( width / 7.680f, height / 1.197f ) );
      wall.add( new PVector( width / 7.245f, height / 1.197f ) );
      wall.add( new PVector( width / 7.471f, height / 1.188f ) );
      wall.add( new PVector( width / 7.059f, height / 1.186f ) );
      wall.add( new PVector( width / 6.931f, height / 1.188f ) );
      wall.add( new PVector( width / 6.809f, height / 1.179f ) );
      wall.add( new PVector( width / 7.059f, height / 1.169f ) );
      wall.add( new PVector( width / 7.471f, height / 1.171f ) );
      wall.add( new PVector( width / 7.191f, height / 1.178f ) );
      wall.add( new PVector( width / 7.619f, height / 1.182f ) );
      wall.add( new PVector( width / 8.101f, height / 1.175f ) );
      wall.add( new PVector( width / 8.348f, height / 1.169f ) );
      wall.add( new PVector( width / 8.649f, height / 1.165f ) );
      wall.add( new PVector( width / 8.384f, height / 1.178f ) );
      wall.add( new PVector( width / 8.930f, height / 1.184f ) );
      wall.add( new PVector( width / 9.275f, height / 1.188f ) );
      wall.add( new PVector( width / 9.275f, height / 1.204f ) );
      wall.add( new PVector( width / 8.848f, height / 1.201f ) );
      wall.add( new PVector( width / 8.458f, height / 1.188f ) );
      wall.add( new PVector( width / 7.934f, height / 1.186f ) );
      wall.add( new PVector( width / 8.101f, height / 1.201f ) );
      wall.add( new PVector( width / 8.848f, height / 1.204f ) );
      wall.add( new PVector( width / 9.600f, height / 1.219f ) );
      wall.add( new PVector( width / 9.505f, height / 1.231f ) );
      break;
    case 42:  //\u718a\u672c
      wall.add( new PVector( width / 6.621f, height / 1.195f ) );
      wall.add( new PVector( width / 6.421f, height / 1.197f ) );
      wall.add( new PVector( width / 6.358f, height / 1.204f ) );
      wall.add( new PVector( width / 6.057f, height / 1.211f ) );
      wall.add( new PVector( width / 5.614f, height / 1.201f ) );
      wall.add( new PVector( width / 5.424f, height / 1.201f ) );
      wall.add( new PVector( width / 5.501f, height / 1.211f ) );
      wall.add( new PVector( width / 5.275f, height / 1.208f ) );
      wall.add( new PVector( width / 5.093f, height / 1.186f ) );
      wall.add( new PVector( width / 5.378f, height / 1.169f ) );
      wall.add( new PVector( width / 5.565f, height / 1.160f ) );
      wall.add( new PVector( width / 5.408f, height / 1.146f ) );
      wall.add( new PVector( width / 5.455f, height / 1.132f ) );
      wall.add( new PVector( width / 6.057f, height / 1.126f ) );
      wall.add( new PVector( width / 6.254f, height / 1.132f ) );
      wall.add( new PVector( width / 6.809f, height / 1.132f ) );
      wall.add( new PVector( width / 6.154f, height / 1.171f ) );
      wall.add( new PVector( width / 6.621f, height / 1.165f ) );
      wall.add( new PVector( width / 6.621f, height / 1.171f ) );
      wall.add( new PVector( width / 6.214f, height / 1.182f ) );
      wall.add( new PVector( width / 6.465f, height / 1.188f ) );
      wall.add( new PVector( width / 6.621f, height / 1.195f ) );
      break;
    case 43:  //\u5927\u5206
      wall.add( new PVector( width / 5.731f, height / 1.208f ) );
      wall.add( new PVector( width / 5.783f, height / 1.231f ) );
      wall.add( new PVector( width / 5.614f, height / 1.240f ) );
      wall.add( new PVector( width / 5.275f, height / 1.243f ) );
      wall.add( new PVector( width / 5.275f, height / 1.253f ) );
      wall.add( new PVector( width / 4.936f, height / 1.250f ) );
      wall.add( new PVector( width / 4.741f, height / 1.260f ) );
      wall.add( new PVector( width / 4.582f, height / 1.257f ) );
      wall.add( new PVector( width / 4.518f, height / 1.246f ) );
      wall.add( new PVector( width / 4.627f, height / 1.233f ) );
      wall.add( new PVector( width / 4.776f, height / 1.231f ) );
      wall.add( new PVector( width / 4.800f, height / 1.222f ) );
      wall.add( new PVector( width / 4.344f, height / 1.222f ) );
      wall.add( new PVector( width / 4.444f, height / 1.208f ) );
      wall.add( new PVector( width / 4.267f, height / 1.205f ) );
      wall.add( new PVector( width / 4.364f, height / 1.199f ) );
      wall.add( new PVector( width / 4.248f, height / 1.195f ) );
      wall.add( new PVector( width / 4.295f, height / 1.184f ) );
      wall.add( new PVector( width / 4.394f, height / 1.179f ) );
      wall.add( new PVector( width / 4.518f, height / 1.186f ) );
      wall.add( new PVector( width / 4.660f, height / 1.182f ) );
      wall.add( new PVector( width / 4.717f, height / 1.178f ) );
      wall.add( new PVector( width / 5.066f, height / 1.188f ) );
      wall.add( new PVector( width / 5.161f, height / 1.205f ) );
      wall.add( new PVector( width / 5.333f, height / 1.211f ) );
      wall.add( new PVector( width / 5.565f, height / 1.212f ) );
      wall.add( new PVector( width / 5.486f, height / 1.205f ) );
      wall.add( new PVector( width / 5.731f, height / 1.208f ) );
      break;
    case 44:  //\u5bae\u5d0e
      wall.add( new PVector( width / 6.019f, height / 1.124f ) );
      wall.add( new PVector( width / 5.378f, height / 1.134f ) );
      wall.add( new PVector( width / 5.378f, height / 1.144f ) );
      wall.add( new PVector( width / 5.533f, height / 1.159f ) );
      wall.add( new PVector( width / 5.093f, height / 1.184f ) );
      wall.add( new PVector( width / 4.717f, height / 1.178f ) );
      wall.add( new PVector( width / 4.528f, height / 1.186f ) );
      wall.add( new PVector( width / 4.424f, height / 1.175f ) );
      wall.add( new PVector( width / 4.638f, height / 1.150f ) );
      wall.add( new PVector( width / 4.861f, height / 1.118f ) );
      wall.add( new PVector( width / 4.873f, height / 1.095f ) );
      wall.add( new PVector( width / 5.000f, height / 1.087f ) );
      wall.add( new PVector( width / 5.000f, height / 1.076f ) );
      wall.add( new PVector( width / 5.093f, height / 1.076f ) );
      wall.add( new PVector( width / 5.304f, height / 1.081f ) );
      wall.add( new PVector( width / 5.203f, height / 1.092f ) );
      wall.add( new PVector( width / 5.455f, height / 1.095f ) );
      wall.add( new PVector( width / 5.749f, height / 1.103f ) );
      wall.add( new PVector( width / 5.731f, height / 1.112f ) );
      wall.add( new PVector( width / 6.019f, height / 1.124f ) );
      break;
    case 45:  //\u9e7f\u5150\u5cf6
      wall.add( new PVector( width / 7.328f, height / 1.138f ) );
      wall.add( new PVector( width / 7.328f, height / 1.130f ) );
      wall.add( new PVector( width / 7.138f, height / 1.126f ) );
      wall.add( new PVector( width / 6.809f, height / 1.130f ) );
      wall.add( new PVector( width / 6.295f, height / 1.130f ) );
      wall.add( new PVector( width / 6.115f, height / 1.124f ) );
      wall.add( new PVector( width / 5.783f, height / 1.112f ) );
      wall.add( new PVector( width / 5.783f, height / 1.103f ) );
      wall.add( new PVector( width / 5.455f, height / 1.092f ) );
      wall.add( new PVector( width / 5.275f, height / 1.090f ) );
      wall.add( new PVector( width / 5.304f, height / 1.081f ) );
      wall.add( new PVector( width / 5.533f, height / 1.081f ) );
      wall.add( new PVector( width / 5.533f, height / 1.076f ) );
      wall.add( new PVector( width / 5.378f, height / 1.070f ) );
      wall.add( new PVector( width / 5.647f, height / 1.060f ) );
      wall.add( new PVector( width / 6.154f, height / 1.053f ) );
      wall.add( new PVector( width / 6.154f, height / 1.057f ) );
      wall.add( new PVector( width / 5.926f, height / 1.062f ) );
      wall.add( new PVector( width / 5.926f, height / 1.076f ) );
      wall.add( new PVector( width / 6.095f, height / 1.083f ) );
      wall.add( new PVector( width / 6.254f, height / 1.087f ) );
      wall.add( new PVector( width / 6.194f, height / 1.092f ) );
      wall.add( new PVector( width / 6.019f, height / 1.090f ) );
      wall.add( new PVector( width / 5.872f, height / 1.094f ) );
      wall.add( new PVector( width / 6.194f, height / 1.100f ) );
      wall.add( new PVector( width / 6.508f, height / 1.087f ) );
      wall.add( new PVector( width / 6.508f, height / 1.074f ) );
      wall.add( new PVector( width / 6.194f, height / 1.065f ) );
      wall.add( new PVector( width / 6.295f, height / 1.060f ) );
      wall.add( new PVector( width / 6.575f, height / 1.062f ) );
      wall.add( new PVector( width / 6.621f, height / 1.067f ) );
      wall.add( new PVector( width / 7.191f, height / 1.068f ) );
      wall.add( new PVector( width / 7.529f, height / 1.076f ) );
      wall.add( new PVector( width / 7.191f, height / 1.076f ) );
      wall.add( new PVector( width / 6.882f, height / 1.087f ) );
      wall.add( new PVector( width / 7.413f, height / 1.098f ) );
      wall.add( new PVector( width / 7.471f, height / 1.109f ) );
      wall.add( new PVector( width / 7.245f, height / 1.112f ) );
      wall.add( new PVector( width / 7.328f, height / 1.126f ) );
      wall.add( new PVector( width / 7.619f, height / 1.129f ) );
      wall.add( new PVector( width / 7.559f, height / 1.138f ) );
      wall.add( new PVector( width / 7.328f, height / 1.138f ) );
      break;
    case 46:  //\u6c96\u7e04
      wall.add( new PVector( width / 27.429f, height / 1.050f ) );
      wall.add( new PVector( width / 23.415f, height / 1.050f ) );
      wall.add( new PVector( width / 21.333f, height / 1.044f ) );
      wall.add( new PVector( width / 16.696f, height / 1.058f ) );
      wall.add( new PVector( width / 15.360f, height / 1.050f ) );
      wall.add( new PVector( width / 16.696f, height / 1.044f ) );
      wall.add( new PVector( width / 28.235f, height / 1.032f ) );
      wall.add( new PVector( width / 28.235f, height / 1.025f ) );
      wall.add( new PVector( width / 33.103f, height / 1.022f ) );
      wall.add( new PVector( width / 32.000f, height / 1.015f ) );
      wall.add( new PVector( width / 40.000f, height / 1.010f ) );
      wall.add( new PVector( width / 50.526f, height / 1.018f ) );
      wall.add( new PVector( width / 38.400f, height / 1.025f ) );
      wall.add( new PVector( width / 38.400f, height / 1.032f ) );
      wall.add( new PVector( width / 24.000f, height / 1.039f ) );
      wall.add( new PVector( width / 27.429f, height / 1.050f ) );
      break;
    default:
  }
  return wall;
}
class Phases{
  private int shareKnowledgeSrc;
  
  Phases(){}
  
  public void gameMainPhase(){
    ImageSet images;
    player p = players.get( draws.targetPlayerNo );
    int i, prePosition = p.position;
    prefecture t;
    IntList lst = todoufuken.get( p.position ).adjacent;  //\u81ea\u52d5\u8eca\u307e\u305f\u306f\u8239\u306b\u3088\u308b\u79fb\u52d5
    
    ////\u81ea\u52d5\u8eca\u307e\u305f\u306f\u8239\u306b\u3088\u308b\u79fb\u52d5
    for( i = 0; i < lst.size(); i++ ){
      t = todoufuken.get( lst.get( i ) );
      if( t.isHit() && p.position != t.position ){
        p.move( t.position );
        ACTION_COUNT--;
      }
    }
    
    ////\u30b7\u30e3\u30c8\u30eb\u4fbf\u306b\u3088\u308b\u79fb\u52d5
    for( i = 0; i < gameStatus.researchStationList.size(); i++ ){
      t = todoufuken.get( gameStatus.researchStationList.get( i ) );
      if( t.isHit() && p.position != gameStatus.researchStationList.get( i ) ){
        p.move( t.position );
        ACTION_COUNT--;
      }
    }
    
    ////\u76f4\u884c\u4fbf\u306b\u3088\u308b\u79fb\u52d5
    if( !gameStatus.charterFlightFlag || !gameStatus.airliftFlag ){
      for( i = 0; i < p.cards.size(); i++ ){
        t = todoufuken.get( p.cards.get( i ) );
        if( t.isHit() && p.position != p.cards.get( i ) ){
          p.move( t.position );
          p.removeCard( p.cards.get( i ) );
          ACTION_COUNT--;
        }
      }
    }
    
    ////\u30c1\u30e3\u30fc\u30bf\u30fc\u4fbf\u306b\u3088\u308b\u79fb\u52d5&\u4f5c\u6226\u30a8\u30ad\u30b9\u30d1\u30fc\u30c8\u306e\u7279\u6b8a\u6280\u80fd\u3001\u30a4\u30d9\u30f3\u30c8\u30ab\u30fc\u30c9"\u7a7a\u8f38"\u3001"\u653f\u5e9c\u306e\u88dc\u52a9"
    if( gameStatus.charterFlightFlag || gameStatus.airliftFlag ){
      for( prefecture tt : todoufuken ){
        if( tt.isHit() && p.position != tt.position ){
          p.move( tt.position );
          if( draws.selectCard.size() <= 0 ){ 
            p.removeCard( p.position );
          }else{
            players.get( MAIN_TURN.n ).removeCard( draws.selectCard.get( 0 ) );
          }
          if( gameStatus.charterFlightFlag ){
            ACTION_COUNT--;
          }
          gameStatus.resetSetting();
        }//if
      }//for
    }//if
    if( playerActionImgSet.get(0).isHit() && searchList( p.cards, p.position ) ){
      gameStatus.charterFlightFlag = true;
    }
        
    ////\u8abf\u67fb\u57fa\u5730\u306e\u8a2d\u7f6e
    if( playerActionImgSet.get(1).isHit() && todoufuken.get( p.position ).researchStation != true ){
      if( p.role == ROLES.OPERATIONS_EXPERT ){
         phase = PHASE.SPECIAL_SKILL;
      }else if( searchList( p.cards, p.position ) ){
        todoufuken.get( p.position ).setResearchStation();
        p.removeCard( p.position );
        ACTION_COUNT--;
      }
    }
    
    ////\u611f\u67d3\u8005\u306e\u6cbb\u7642
    int[] cnt = {0,0,0,0};
    for( i = 0; i < TreatDiseases.size(); i++ ){
      images = TreatDiseases.get( i );
      if( images.isHit() ){
        if( gameStatus.cureMarkersFlag[i] >= 1 || p.role == ROLES.MEDIC ){
          addPathogenProcess( p.position, i, CURE );
        }else{
          addPathogenProcess( p.position, i, -1 );
        }
        ACTION_COUNT--;
      }
      //\u6cbb\u7642\u85ac\u304c\u4f5c\u6210\u3055\u308c\u305f\u72b6\u614b\u3067\u8077\u696d\u304c\u885b\u751f\u5175\u306e\u6642\u3001\u30a2\u30af\u30b7\u30e7\u30f3\u3092\u6d88\u8cbb\u305b\u305a\u306b\u6cbb\u7642\u3059\u308b
      if( p.role == ROLES.MEDIC && gameStatus.cureMarkersFlag[i] >= 1 && p.position != prePosition && todoufuken.get( p.position ).pathogenCnt[i] >= 1 ){
        addPathogenProcess( p.position, i, CURE );
      }
      //\u75c5\u539f\u4f53\u306e\u6570\u3092\u30ab\u30a6\u30f3\u30c8\u3059\u308b
      for( int j = 0; j < todoufuken.size() - eventCardNum; j++ ){
        t = todoufuken.get( j );
        if( t.col == i ){
          cnt[i] += t.pathogenCnt[t.col];
        }
      }
      //\u75c5\u539f\u4f53\u304c0\u3067\u3001\u6cbb\u7642\u85ac\u304c\u4f5c\u6210\u3055\u308c\u3066\u3044\u308b\u6642
      if( cnt[i] == 0 && gameStatus.cureMarkersFlag[i] == 1 ){
        gameStatus.cureMarkersFlag[i] = 2;
      }
    }
    
    ////\u77e5\u8b58\u306e\u5171\u6709
    if( playerActionImgSet.get( 2 ).isHit() ){
      shareKnowledgeSrc = -1;
      draws.displayPlayer.clear();
      for( i = 0; i < players.size(); i++ ){
        if( searchList( players.get( i ).cards, p.position ) ){
          shareKnowledgeSrc = i;
        }else if( p.position == players.get( i ).position ){
          draws.displayPlayer.append( i );
        }
      }//for
      if( shareKnowledgeSrc != -1 &&  draws.displayPlayer.size() != 0 ){
        //\u5bfe\u8c61\u306e\u30ab\u30fc\u30c9\u3092\u6301\u3063\u3066\u3044\u308b\u306e\u304c\u4ed6\u306e\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u5834\u5408\u3001\u81ea\u5206\u3060\u3051\u3092\u5bfe\u8c61\u306b\u3059\u308b
        if( shareKnowledgeSrc != p.no ){
          draws.displayPlayer.clear();
          draws.displayPlayer.append( p.no );
        }//if
        gameStatus.pressFlag = false;
        phase = PHASE.SHARE_KNOWLEDGE;
      }//if
    }//if
    
    ////\u6cbb\u7642\u85ac\u306e\u767a\u898b
    if( p.role == ROLES.SCIENTIST ){
      gameStatus.discoverCureCnt = 4;  //\u79d1\u5b66\u8005\u306f4\u679a\u3067\u6cbb\u7642\u85ac\u3092\u4f5c\u6210\u53ef\u80fd
    }else{
      gameStatus.discoverCureCnt = 5;  //\u4ed6\u306f5\u679a\u3067\u6cbb\u7642\u85ac\u3092\u4f5c\u6210\u53ef\u80fd
    }
    if( playerActionImgSet.get( 3 ).isHit() & p.cards.size() >= gameStatus.discoverCureCnt && searchList( gameStatus.researchStationList, p.position ) ){
      phase = PHASE.DISCOVER_A_CURE;
      draws.targetPlayerNo = MAIN_TURN.n;
      gameStatus.pressFlag = false;
    }
    
    ////\u30a4\u30d9\u30f3\u30c8\u30ab\u30fc\u30c9\u306e\u4f7f\u7528
    if( playerActionImgSet.get( 4 ).isHit() && searchList( p.cards, 47, 48, 49, 50, 51 ) ){
      phase = PHASE.EVENT_CARD;
      gameStatus.pressFlag = false;
      draws.targetPlayerNo = MAIN_TURN.n;
    }
    
    ////\u7279\u6b8a\u6280\u80fd\u306e\u767a\u52d5
    if( playerActionImgSet.get( 5 ).isHit() && p.playerSkillFlag ){
      draws.targetPlayerNo = MAIN_TURN.n;
      switch( p.role ){
        case RESEARCHER:
          for( i = 0; i < players.size(); i++ ){
            if( p.no != i && p.position == players.get( i ).position ){
              draws.displayPlayer.append( i );
            }
          }
        break;
        case DISPATCHER:
          for( i = 0; i < players.size(); i++ ){
            draws.displayPlayer.append( i );
          }
        break;
        default:
      }
      phase = PHASE.SPECIAL_SKILL;
    }
    
    ////\u30d8\u30eb\u30d7
    if( rectHit( width - RECT_SIZE * 0.5f, 0, RECT_SIZE * 0.5f, RECT_SIZE * 0.5f ) ){
      gameStatus.helpFlag = !gameStatus.helpFlag;
    }
  }//gameMainPhase
    
  public void gameInputPhase(){
    int i,j;
    
    switch( phase ){
      case GAME_DIFFICULTY_INPUT:  //\u30a8\u30d4\u30c7\u30df\u30c3\u30af\u30ab\u30fc\u30c9\u306e\u679a\u6570\u306e\u8a2d\u5b9a
        if( 51 < key && key < 55 ){ // key -> '4' to '6'
          epidemicCardNumber = PApplet.parseInt(key) - 48;
          phase = PHASE.PLAYER_NUMBER_INPUT;
        }
      break;  //GAME_DIFFICULTY_INPUT
      case PLAYER_NUMBER_INPUT:  //\u30d7\u30ec\u30a4\u30e4\u30fc\u4eba\u6570\u306e\u8a2d\u5b9a
        if( 49 < key && key < 53 ){ // key -> '2' to '4'
          playerNumber = PApplet.parseInt(key) - 48;
          phase = PHASE.PLAYER_NAME_INPUT;
        }
      break;  //PLAYER_NUMBER
      case PLAYER_NAME_INPUT:    //\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u540d\u524d\u306e\u8a2d\u5b9a
        if( keyCode == BACKSPACE ){  //1\u6587\u5b57\u524a\u9664
          if( charLst.size() > 0 ){
            charLst.remove( charLst.size() - 1 );
          }
          return;
        }
        if( keyCode != SHIFT && keyCode != RETURN && keyCode != ENTER ){  //1\u6587\u5b57\u8ffd\u52a0
          String k = ""; k += key;
          charLst.append( k );
          return;
        }
        
        if( ( keyCode == RETURN || keyCode == ENTER ) && charLst.size() >= 1 ){  //\u6c7a\u5b9a
          String str = "";
          for( i = 0; i < charLst.size(); i++ ){
            str += charLst.get( i );
          }
          playerNames.append( str );
          charLst.clear();
          if( playerNames.size() >= playerNumber ){
            int rnd, rndMaxNum = 7;
            //           \u5371\u6a5f\u7ba1\u7406\u5b98                 \u901a\u4fe1\u6307\u4ee4\u54e1             \u4f5c\u6226\u30a8\u30ad\u30b9\u30d1\u30fc\u30c8   \u885b\u751f\u5175       \u79d1\u5b66\u8005          \u7814\u7a76\u54e1           \u691c\u75ab\u5b98
            ROLES[] r = {ROLES.CONTINGENCY_PLANNER,ROLES.DISPATCHER,ROLES.OPERATIONS_EXPERT,ROLES.MEDIC,ROLES.SCIENTIST,ROLES.RESEARCHER,ROLES.QUARANTINE_SPECIALIST};
            TEXT_SIZE = bs.y * 0.125f;  //\u30c6\u30ad\u30b9\u30c8\u30b5\u30a4\u30ba\u518d\u8a2d\u5b9a
            textSize( TEXT_SIZE );
            players.clear();
            for( i = 0; i < playerNumber; i++ ){
              rnd = floor( random( 0, rndMaxNum ) );
              players.add( new player( playerNames.get( i ), 12, todoufuken.get( 12 ).x, todoufuken.get( 12 ).y, i, r[rnd] ) );  //\u30d7\u30ec\u30a4\u30e4\u30fc\u306e\u540d\u524d\u3068\u4f4d\u7f6e\u8a2d\u5b9a
              r[rnd] = r[--rndMaxNum];
            }
            
            //\u30d7\u30ec\u30a4\u30e4\u30fc\u30ab\u30fc\u30c9\u306e\u8a2d\u5b9a
            IntList pDeck = new IntList();
            
            //0 to 51 \u30e9\u30f3\u30c0\u30e0\u306b\u8a2d\u5b9a
            pDeck = setList( todoufukenNum + eventCardNum );
            
            //\u30d7\u30ec\u30a4\u30e4\u30fc\u4eba\u6570\u306b\u3088\u3063\u3066\u521d\u671f\u306e\u30ab\u30fc\u30c9\u3092\u914d\u308b\u679a\u6570\u3092\u5909\u66f4
            int playerCardNum = 0;
            switch( playerNumber ){
              case 2:  playerCardNum = 4;  break;
              case 3:  playerCardNum = 3;  break;
              case 4:  playerCardNum = 2;  break;
            }
            
            //\u30d7\u30ec\u30a4\u30e4\u30fc\u306b\u30ab\u30fc\u30c9\u3092\u30bb\u30c3\u30c8
            for( player p : players ){
              for( i = 0; i < playerCardNum; i++ ){
                p.setCard( pDeck.remove( pDeck.size() - 1 ) );
              }
            }
            
            //\u30d7\u30ec\u30a4\u30e4\u30fc\u30c7\u30c3\u30ad\u306b\u30a8\u30d4\u30c7\u30df\u30c3\u30af\u30ab\u30fc\u30c9\u3092\u8a2d\u5b9a
            IntList rndNum = new IntList();
            int cnt = floor( pDeck.size() / epidemicCardNumber );  //\u30d7\u30ec\u30a4\u30e4\u30fc\u30c7\u30c3\u30ad\u306e\u6b8b\u308a\u3092\u30a8\u30d4\u30c7\u30c3\u30af\u30ab\u30fc\u30c9\u679a\u6570\u3067\u5206\u5272\u3057\u305f\u6570
            int amari = pDeck.size() % epidemicCardNumber;         //\u2191\u306e\u4f59\u308a
            
            for( i = 0; i < epidemicCardNumber; i++ ){             //\u30a8\u30d4\u30c7\u30c3\u30af\u30ab\u30fc\u30c9\u679a\u6570\u56de\u30eb\u30fc\u30d7
              if( i == epidemicCardNumber - 1 ){                   //\u6700\u5f8c\u306e\u30eb\u30fc\u30d7\u3060\u3051\u4f59\u308a\u3092\u8db3\u3059
                cnt += amari;
              }
              rndNum.append( i + 52 );                             //\u30a8\u30d4\u30c7\u30df\u30c3\u30af\u30ab\u30fc\u30c9\u3092\u30bb\u30c3\u30c8 52 to 57
              for( j = 0; j < cnt; j++ ){                          //(\u5206\u5272\u6570 + \u30a8\u30d4\u30c7\u30df\u30c3\u30af\u30ab\u30fc\u30c91\u3064)\u56de\u30eb\u30fc\u30d7
                rndNum.append( pDeck.remove( pDeck.size() - 1 ) ); //\u5909\u6570\u306b\u5206\u5272\u3057\u305f\u6570\u3092\u5165\u308c\u308b
              }
              rndNum.shuffle();                                    //\u30b7\u30e3\u30c3\u30d5\u30eb
              for( j = 0; j < rndNum.size(); j++ ){                //(\u5206\u5272\u6570 + \u30a8\u30d4\u30c7\u30df\u30c3\u30af\u30ab\u30fc\u30c91\u3064)\u56de\u30eb\u30fc\u30d7
                gameStatus.playerDeck.append( rndNum.get( j ) );   //\u30b7\u30e3\u30c3\u30d5\u30eb\u3057\u305f\u5024\u3092\u30c7\u30c3\u30ad\u306b\u30bb\u30c3\u30c8
              }
              rndNum.clear();                                      //\u5909\u6570\u3092\u30af\u30ea\u30a2
            }
            
            //\u611f\u67d3\u30ab\u30fc\u30c9\u306e\u8a2d\u5b9a
            int count = 3, setCard;
            prefecture t;
            for( i = 0; i < 9; i++ ){
              setCard = removeAndAppendList( gameStatus.infectionDiscardPile, gameStatus.infectionDeck, gameStatus.infectionDeck.size() - 1 );
              t = todoufuken.get( setCard );
              t.pathogenCnt[t.col] = count;
              if( i % 3 == 2 ){  //3(0 to 2) 2(3 to 5) 1(6 to 8)
                 count--;
              }
            }
            gameStatus.helpFlag = !gameStatus.helpFlag;
            phase = PHASE.GAME;  //\u30d5\u30a7\u30a4\u30ba\u30c1\u30a7\u30f3\u30b8
          }//if
        }//if
      break;  //PLAYER_NAME_INPUT
    }
  }
}
  public void settings() {  fullScreen();  noSmooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "PANDEMIC" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
