class prefecture{
  private String name;                      //都道府県名
  private int position;                     //ポジション
  private float x;                          //x座標
  private float y;                          //y座標
  private ArrayList<PVector> wall = new ArrayList<PVector>();  //ディスプレイ用壁
  private int col;                          //病原体色
  private int[] pathogenCnt = new int[4];   //病原体カウント 0青 1赤 2青緑 3紫
  private IntList adjacent = new IntList(); //隣接する都道府県情報
  private boolean researchStation;          //拠点有無
  
  prefecture( String name, int position, float x, float y, ArrayList<PVector> wall, int col, int pathogenCnt, IntList adjacent, boolean researchStation ){
    this.name = name;
    this.position = position;
    this.x = x;
    this.y = y;
    this.wall = wall;
    this.col = col;
    this.pathogenCnt[0] = pathogenCnt;  //青
    this.pathogenCnt[1] = pathogenCnt;  //赤
    this.pathogenCnt[2] = pathogenCnt;  //青緑
    this.pathogenCnt[3] = pathogenCnt;  //紫
    this.adjacent = adjacent;
    this.researchStation = researchStation;
  }
  
  int display( boolean moveFlag ){
    int i, ret = -1;
    
    //移動可能は色を薄くする
    if( moveFlag ){
      fill( pathogenColorPattern[col], 100 );
    }else{
      fill( pathogenColorPattern[col] ); 
    }
    
    //選択中のマスの色と位置情報
    if( isHit() ){
      fill( WHITE );
      ret = position;
    }
    
    //マスの表示
    stroke( BLACK );
    beginShape();
    for( i = 0; i < wall.size(); i++ ){
      vertex( wall.get( i ).x, wall.get( i ).y );
    }
    endShape();
    
    //調査基地があれば表示
    if( researchStation == true ){
      boardImgSet.get(3).display( x + TEXT_SIZE * 0.5, y - TEXT_SIZE * 0.5, ELLIPSE_SIZE * 0.5, ELLIPSE_SIZE * 0.5 );
    }
    
    //マスに名前を入れる
    fill( BLACK );
    textSize( TEXT_SIZE );
    text( name, x, y );
    
    return ret;
  }
  
  void pathogenDisplay(){
    int i, j;
    float pathogenSize = ( width + height ) / 300, offset = 0;
    
    //マスの左に病原体マーカーセット( 0と3は1個分右に移動)
    for( i = 0; i < pathogenCnt.length; i++ ){
      fill( pathogenColorPattern[i] );
      if( i == 0 || i == 3 ){
        offset = pathogenSize;
      }else{
        offset = 0;
      }
      for( j = 0; j < pathogenCnt[i]; j++ ){
        rect( x - ( pathogenSize * 2.5 ) - ( j * pathogenSize ) + offset, y - ( pathogenSize * 2 ) + ( pathogenSize * i ), pathogenSize, pathogenSize );
      }
    }
  }
  
  boolean setResearchStation(){
    if( researchStation == false ){
      researchStation = true;
      gameStatus.researchStationList.append( position );
      return true;
    }else{
      return false;
    }
  }
  
  boolean isHit(){
    int hitCnt = 0,st,sp;
    for( int hitLineNo = 0; hitLineNo < gameStatus.hitLine.size(); hitLineNo++ ){  //判定用線
      for( int lineNo = 0; lineNo < wall.size() - 1; lineNo++ ){  //線
        st = lineNo; sp = lineNo + 1;
        //判定線と壁の交差判定
        if( isCollisionSide( wall.get( st ), wall.get( sp ), gameStatus.centerPoint, gameStatus.hitLine.get( hitLineNo ) ) && 
            isCollisionSide( gameStatus.centerPoint, gameStatus.hitLine.get( hitLineNo ), wall.get( st ), wall.get( sp ) ) ){
          hitCnt++;
          break;
        }
      }
    }
    //上下左右の判定線と壁が交差していたら
    if( hitCnt == 4 ){
      return true;
    }else{
      return false;
    }
  }
  
  boolean isCollisionSide( PVector r1, PVector r2, PVector p1, PVector p2 ){
    float   t1, t2;
    
    //衝突判定計算
    t1 = ( r1.x - r2.x ) * ( p1.y - r1.y ) + ( r1.y - r2.y ) * ( r1.x - p1.x );
    t2 = ( r1.x - r2.x ) * ( p2.y - r1.y ) + ( r1.y - r2.y ) * ( r1.x - p2.x );
    
    //それぞれの正負が異なる（積が負になる）か、0（点が直線上にある）
    //ならクロスしている
    if( t1 * t2 < 0 || t1 == 0 || t2 == 0 ){
      return true; //クロスしている
    }else{
      return false; //クロスしない
    }
  }
}

class player{
  private String name;                    //名前
  private int position;                   //ポジション
  private float x;                        //x
  private float y;                        //y
  private int no;                         //ナンバー
  private ROLES role;                     //役職
  private boolean playerSkillFlag;        //プレイヤースキル有無
  private color col;                      //色
  private IntList cards = new IntList();  //手持ちカード
  
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
  
  void setCard( int card ){
    cards.append( card );
    if( cards.size() >= 8 ){
      phase = PHASE.HAND_LIMIT;
      draws.targetPlayerNo = no;
      gameStatus.pressFlag = false;
    }
    cards.sort();
  }
  
  int removeCard( int card ){
    for( int i = 0; i < cards.size(); i++ ){
      if( card == cards.get(i) ){
        gameStatus.playerDiscardPile.append( card );
        return cards.remove( i );
      }
    }
    return -1;
  }
  
  void display(){
    prefecture t;
    
    //プレイヤーのコマ表示
    float resize = ELLIPSE_SIZE / 3;
    float px = x + resize / 2, py = y + resize / 2;
    if( MAIN_TURN.n == no ){
      fill( WHITE );
    }else{
      fill( col );
    }
    strokeWeight( 1 );
    triangle( px, py, px + resize / 1.5, py + resize * 1.5, px - resize / 1.5, py + resize * 1.5 );
    ellipse ( px, py, resize, resize );
    fill( BLACK );
    
    //プレイヤーの手札の表示
    textAlign( LEFT, CENTER );
    float wid = 1.125 * bs.x;
    if( MAIN_TURN.n == no ){
      stroke( RED );
      strokeWeight( 3 );
    }else{
      noStroke();
    }
    fill( col, 100 );
    rect( 7.875 * bs.x - ( no * wid ), 7 * bs.y, wid, 2 * bs.y );
    fill( BLACK );
    textSize( TEXT_SIZE * 2.5 );
    text( name, 8 * bs.x - ( no * wid ), 7.125 * bs.y );
    textSize( TEXT_SIZE * 1.5 );
    stroke( BLACK );
    strokeWeight( 1 );
    for( int i = 0; i < cards.size(); i++ ){
      t = todoufuken.get( cards.get(i) );
      fill( BLACK );
      text( t.name, 8 * bs.x - ( no * wid ), 7.5 * bs.y + i * ( TEXT_SIZE * 1.5 ) );
      fill( pathogenColorPattern[t.col] );
      rect( 7.875 * bs.x - ( no * wid ), 7.5 * bs.y + i * ( TEXT_SIZE * 1.5 ) - TEXT_SIZE * 0.25, TEXT_SIZE, TEXT_SIZE );
    }
    textSize( TEXT_SIZE );
    textAlign( CENTER, CENTER );
  }
  
  void move( int pos ){
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
  
  void display(){
    image( image, x, y, w, h ); 
  }
  
  void display( float x, float y, float w, float h ){
    image( image, x, y, w, h );
  }
  
  boolean isHit(){
    return ( x < mouseX && mouseX < x + w  && y < mouseY && mouseY < y + h );
  }
}//ImageSet

class GAME_STATUS{
  private boolean pressFlag;                               //アクションを実行した時に一度マウスのボタンを離さないと選択できないようにするため
  private boolean helpFlag = false;                        //ヘルプを表示するかどうか
  private boolean gameOverFlag;                            //ゲームオーバーの管理
  private boolean charterFlightFlag = false;               //チャーター便による移動
  private boolean airliftFlag = false;                     //イベントカード"空輸"を使用した時
  private boolean oneQuietNightFlag = false;               //イベントカード"静かな夜"を使用した時
  private boolean governmentGrantFlag = false;             //イベントカード"政府の補助"を使用した時
  
  private int[] infectionRateTrack = {2,2,2,3,3,4,4};      //感染カードのドロー枚数
  private int irtCnt = 0;                                  //↑用の変数
  
  private IntList playerDeck = new IntList();              //都道府県( 0 to 46 ) イベントカード( 47 to 51 ) エピデックカード( 52 to 57 )
  private IntList playerDiscardPile = new IntList();       //プレイヤーカード捨て札
  private IntList infectionDeck = new IntList();           //感染カードデッキ
  private IntList infectionDiscardPile = new IntList();    //感染カード捨て札
  private IntList exclusionInfectionCard = new IntList();  //除外された感染カード用
  private int drawCardNum = 0;                             // 2 - drawCardNum = ターン終了時プレイヤーがドローする枚数
  private String epidemicCardName;                         //エピデミックカード処理で引いた感染カードの名前
  
  private IntList researchStationList = new IntList();     //調査基地が置いてある場所の配列
  private int[] cureMarkersFlag = {0,0,0,0};               //作成した治療薬のフラグ 0:未 1:済 2:根絶
  private int discoverCureCnt = 5;                         //治療薬を作成するのに必要な都市カードの枚数
  
  private IntList outbreakLoopList = new IntList();        //アウトブレイク処理用
  private int outBreakCount = 0;                           //アウトブレイクが起こった回数
  
  private PVector centerPoint = new PVector();             //多角形判定用
  private ArrayList<PVector> hitLine = new ArrayList<PVector>();

  GAME_STATUS(){}
  
  void resetSetting(){
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
  
  void gameOverSettingReset(){
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
  
  void update(){
    //位置更新
    centerPoint.set( mouseX, mouseY );      //中心点
    hitLine.get( 0 ).set( mouseX, 0      ); //上
    hitLine.get( 1 ).set( mouseX, height ); //下
    hitLine.get( 2 ).set( 0     , mouseY ); //左
    hitLine.get( 3 ).set( width , mouseY ); //右
  }
}