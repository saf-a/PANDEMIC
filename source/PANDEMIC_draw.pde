class Draws{                       
  private float infectionCardDrawFrame = 0;                //感染カードをドローした時の演出用
  private float discoverCureDrawFrame = 0;                 //"治療薬の作成"ドローした時の演出用
  private boolean discoverCureSuccessFlag;                 //治療薬を作成できた時にtrue
  private float outBreakDrawFrame = 0;                     //アウトブレイクが起こった時の演出用
  private StringList outBreakDrawName = new StringList();  //アウトブレイクが起こった場所の名前
  private float epidemicCardDrawFrame = 0;                 //エピデミックカードをドローした時の演出用
  
  private int targetPlayerNo;                              //対象のプレイヤー
  private IntList displayCard = new IntList();             //カード表示用配列
  private IntList selectCard = new IntList();              //選択したカードを格納する配列
  private int scrollCnt = 0;                               //選択したカード枚数が8枚を超える時に使用
  private IntList displayPlayer = new IntList();           //プレイヤー表示用配列
  private IntList selectPlayerNo = new IntList();          //選択したプレイヤーを格納する配列
  
  Draws(){}
  
  void gameMainDraw(){
    int i, j, cnt = 0, pos = -1, ret;
    float x, y;
    player p = players.get( MAIN_TURN.n );
    prefecture t;
    IntList lst = new IntList();  //自動車または船による移動
    getList( todoufuken.get( p.position ).adjacent, lst );
    
    //ボード画像
    tint( WHITE );
    boardImgSet.get( 0 ).display();
    
    ////移動可能なマスの色を変更
    strokeWeight( 2 );
    if( !gameStatus.charterFlightFlag && !gameStatus.airliftFlag  & !gameStatus.governmentGrantFlag ){
      //直行便による移動
      for( i = 0; i < p.cards.size(); i++ ){
        if( p.position != p.cards.get(i) ){
          lst.append( p.cards.get( i ) );
        }
      }
      //シャトル便による移動
      if( searchList( gameStatus.researchStationList, p.position ) ){
        for( i = 0; i < gameStatus.researchStationList.size(); i++ ){
          if( p.position != gameStatus.researchStationList.get( i ) ){
            lst.append( gameStatus.researchStationList.get( i ) );
          }
        }
      }
    }else{
      //チャーター便による移動、イベントカード"空輸"
      lst.clear();
      lst = setList( todoufukenNum );
    }
    
    //マス描写
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
    
    //都道府県上にプレイヤーの表示
    for( player pp : players ){
      pp.display();
    }
    
    //病原体描写
    for( prefecture tt : todoufuken ){
      tt.pathogenDisplay(); 
    }
    
    ////右下にマスの情報を入れる
    //カーソルがマスの上にないならターンプレイヤーの位置情報をセット
    if( pos == -1 ){
      pos = p.position;
    }
    t = todoufuken.get( pos );
    //右下に白枠を表示
    noStroke();
    fill( WHITE, 100 );
    rect( 7.5 * bs.x, 3 * bs.y, 1.5 * bs.x, 3.5 * bs.y );
    //選択中のマスの名前の表示
    float tsx = TEXT_SIZE * 3.7, rSize = TEXT_SIZE * 1.5;
    textSize( tsx );
    stroke( BLACK );
    strokeWeight( 3 );
    for( i = 2; i >= 0; i-- ){
      fill( textColorPattern[i] );
      text( t.name, 7.875 * bs.x + i, 3.25 * bs.y + i );
    }
    //病原体の表示
    x = 7.5; y = 4;
    for( i = 0; i < t.pathogenCnt.length; i++ ){
      fill( pathogenColorPattern[i] );
      for( j = 0; j < t.pathogenCnt[i]; j++ ){
        rect( x * bs.x + ( j + 0.5 ) * rSize, y * bs.y + rSize, rSize, rSize );
      }
      y+=0.5;
    }
    //調査基地の表示
    if( t.researchStation == true ){
      boardImgSet.get(3).display();
    }
    //テキストサイズを元に戻す
    textSize( TEXT_SIZE );
        
    ////左上にアクションカウント、感染カード、プレイヤーカード情報を反映
    tint( WHITE, 100 );
    //アクションカウント
    numbers.get( ACTION_COUNT ).display( 0, 0, bs.x, bs.y );
    //感染カードのドロー枚数
    numbers.get( gameStatus.infectionRateTrack[gameStatus.irtCnt] ).display( 0, bs.y, bs.x, bs.y );
    //アウトブレイク回数
    if( gameStatus.outBreakCount <= 8 ){
      numbers.get( gameStatus.outBreakCount ).display( 0, bs.y * 2, bs.x, bs.y );
    }
    //感染カード
    textSize( TEXT_SIZE * 1.5 );
    boardImgSet.get( 1 ).display();
    cnt = 0; y = 0.5; x = 1.25;
    for( i = 0; i < todoufuken.size() - eventCardNum; i++ ){
      if( searchList( gameStatus.infectionDiscardPile, i ) ){
        fill( BLACK );
      }else if( searchList( gameStatus.exclusionInfectionCard, i ) ){
        fill( cGRAY );
      }else{
        fill( WHITE );
      }
      text( todoufuken.get(i).name, x * bs.x, y * TEXT_SIZE * 1.5 );
      y++;
      cnt++;
      if( cnt == 24 ){
        y = 0.5; x+=0.5;
      }
    }
    //プレイヤーカード
    boardImgSet.get( 2 ).display();
    cnt = 0;  y = 0.5;  x+=0.5;
    for( i = 0; i < todoufuken.size(); i++ ){
      if( searchList( gameStatus.playerDiscardPile, i ) ){
        fill( BLACK ); 
      }else{
        fill( WHITE );
      }

      text( todoufuken.get( i ).name, x * bs.x + TEXT_SIZE, y * TEXT_SIZE * 1.5 );
      cnt++;
      y++;
      if( cnt == 24 ){
        y = 0.5; x += 0.5;
      }else if( cnt == 47 ){
        y = 0.5; x += 0.5;
      }
    }
    textSize( TEXT_SIZE );
    
    //0:チャーター便による移動　1:調査基地の設置 2:治療薬の発見 3:知識の共有 4:イベントカード 5:特殊技能
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
    
    //治療薬の発見
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
    
    //アウトブレイク、治療薬の作成、感染カードをドロー、エピデミックカード用の描写
    if( outBreakDrawFrame > 0 || discoverCureDrawFrame > 0 || infectionCardDrawFrame > 0 || epidemicCardDrawFrame > 0 ){
      specialProductionDraw();
    }else if( gameOverCheck() != GAME_OVER.CONTINUE ){  //ゲーム終了のチェック
      phase = PHASE.GAME_OVER; 
    }
    
  }//gameBoardDraw
  
  void specialProductionDraw(){
    int i;
    float x = 0, y = height * 0.5, ofstX = 1, ofstY = 0;
    
    stroke( BLACK );
    if( epidemicCardDrawFrame-- > 0 ){  //エピデミックカードドロー時の描写
      fill( GREEN, 128 );
      rect( 0, 0, width, height );
      fill( BLACK );
      textSize( TEXT_SIZE * 5 );
      text( "エピデミックカードをドローしました", width * 0.5, height * 0.25 );
      text( gameStatus.epidemicCardName + "に病原体が3つ置かれます", width * 0.5, height * 0.5 );
      text( "感染率が" + gameStatus.infectionRateTrack[gameStatus.irtCnt] + "になりました", width * 0.5, height * 0.75 );
    }else if( outBreakDrawFrame-- > 0 ){  //アウトブレイク時の描写
      x = width / ( ( outBreakDrawName.size() + 3 ) % 4 + 2 );
      fill( RED, 128 );
      rect( 0, 0, width, height );
      fill( BLACK );
      textSize( TEXT_SIZE * 5 );
      text( "アウトブレイクが発生しました", width * 0.5, height * 0.25 );
      for( i = 0; i < outBreakDrawName.size(); i++ ){
        text( outBreakDrawName.get(i), x * ofstX ,y + ofstY );
        ofstX += 1;
        if( i == 3 ){
          ofstX = 1; ofstY = height * 0.25;
        }
      }
    }else if( discoverCureDrawFrame-- > 0 ){  //治療薬の作成時の描写
      color colRect, colTxt;
      String str;
      if( discoverCureSuccessFlag ){
        colRect = WHITE;
        colTxt = BLACK;
        str = "治療薬を作成しました";
      }else{
        colRect = BLACK;
        colTxt = WHITE;
        str = "治療薬を作成することができません";
      }
      fill( colRect, 100 );
      rect( width * 0.25, height  * 0.25, width * 0.5, height * 0.5 );
      fill( colTxt );
      textSize( TEXT_SIZE * 2.5 );
      text( str, width * 0.5, height * 0.5 );
    }else if( infectionCardDrawFrame-- > 0 ){  //感染カードをドローした時の描写
      fill( DARKBLUE, 128 );
      rect( 0, 0, width, height );
      fill( WHITE );
      textSize( TEXT_SIZE * 2.5 );
      text( "病原体1つを以下の都道府県にセットします", width * 0.5, height * 0.375 );
      textSize( TEXT_SIZE * 5 );
      x = 3;
      for( i = 0; i < 2; i++ ){
        text( todoufuken.get( gameStatus.infectionDiscardPile.get( gameStatus.infectionDiscardPile.size() - ( i + 1 ) ) ).name, x * bs.x, height * 0.5 );
        x += 3;
      }
    }
    textSize( TEXT_SIZE );
    
    if( outBreakDrawFrame <= 0 ){
      outBreakDrawName.clear(); 
    }
  }//specialProductionDraw
  
  void cardSelectDraw(){
    int x = 0, y = 3, i, loopCnt;
    float cardX = bs.x * 1.125, cardY = bs.y * 1.125, cardW = bs.x, cardH = bs.y * 2;
    IntList display = new IntList();
    prefecture t;
    boolean hitFlag = false;
    
    //文字サイズを1.5倍に
    textSize( TEXT_SIZE * 1.5 );
    
    //中央に白背景
    fill( WHITE, 100 );
    rect( width / 4, height / 4, width / 2, height / 2 );
    
    ////表示するカードの情報
    //8枚を超えた時
    if( displayCard.size() > 8 ){
      //上下ボタン
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
    }else if( displayCard.size() >= 1 ){  //ディスプレイ用の配列に数字が存在する時
      display = displayCard;
    }else{
      display = players.get( targetPlayerNo ).cards;
    }
    
    for( i = 0; i < display.size(); i++ ){
      t = todoufuken.get( display.get( i ) );
      
      //選択したカードのリストに存在している時、非表示
      if( searchList( selectCard, t.position ) == false ){
        //選択しているかどうか
        if( rectHit( ( x + 2.5 ) * cardX - cardW / 2, y * cardY - cardH / 2, cardW, cardH ) ){
          hitFlag = true;
          //選択中にボタンが押された時
          if( pressButton() ){
            selectCard.append( t.position );
            break;
          }
        }
        
        //手札のカード表示
        //選択:白 非選択:それぞれの色
        if( hitFlag == true ){
          fill( WHITE ); 
        }else{
          fill( pathogenColorPattern[t.col], 200 );
        }
        rect( ( x + 2.5 ) * cardX - cardW / 2, y * cardY - cardH / 2, cardW, cardH );
        
        //名前の表示
        //選択:黒 非選択:白
        if( hitFlag == true ){
          fill( BLACK );
        }else{
          fill( WHITE );
        }
        text( t.name, ( x + 2.5 ) * cardX, y * cardY );
      }
      
      //次
      x++;
      if( i == 3 ){
        y = 5; x = 0;
      }
      hitFlag = false;
    }//for
    
    //文字サイズを元に戻す
    textSize( TEXT_SIZE );
    
  }//cardSelectDraw
  
  void playerSelectDraw(){
    float x = 0, ofst = 0, add = 0;
    float cardW = bs.x, cardH = bs.y * 2;
    boolean flag;
    
    switch( displayPlayer.size() ){
      case 1:  x = width * 0.5; ofst = 1; add = 0;   break;
      case 2:  x = bs.x;        ofst = 3; add = 3;   break;
      case 3:  x = bs.x;        ofst = 3; add = 1.5; break;
      case 4:  x = bs.x;        ofst = 3; add = 1;   break;
    }
    
    //白背景
    fill( WHITE, 100 );
    rect( width * 0.25, height * 0.25, width * 0.5, height * 0.5 );
    textSize( TEXT_SIZE * 2.5 );
    
    for( int i = 0; i < displayPlayer.size(); i++ ){
      if( searchList( selectPlayerNo, i ) == false ){
        if( rectHit( ofst * x - cardW * 0.5, height * 0.5 - cardH * 0.5, cardW, cardH ) ){
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
        rect( ofst * x - cardW * 0.5, height * 0.5 - cardH * 0.5, cardW, cardH );
      
        if( flag ){
          fill( BLACK );
        }else{
          fill( WHITE );
        }
        text( players.get( displayPlayer.get(i) ).name, ofst * x, height * 0.5 );
      }
      ofst+=add;
    }//for    
  }//playerSelectDraw
  
  void helpDraw(){
    int i;
    StringList str = new StringList();
    float wid = 1.125 * bs.x, ts = TEXT_SIZE * 2.5;
    boolean flag = false;
    
    //右上に?マーク
    fill( WHITE );
    rect( width - RECT_SIZE * 0.5, 0, RECT_SIZE * 0.5, RECT_SIZE * 0.5 );
    fill( BLACK );
    textSize( TEXT_SIZE * 2 );
    text( "?", width - RECT_SIZE * 0.25, RECT_SIZE * 0.25 );
    
    if( gameStatus.helpFlag ){
      fill( WHITE, 200 );
      rect( 0, 0, width, height );
      
      str = makeStringList( TEXT_SIZE * 2, rule );
      
      //プレイヤーカード
      for( i = 0; i < players.size(); i++ ){
        player p = players.get( i );
        if( rectHit( 7.875 * bs.x - ( p.no * wid ), 7 * bs.y, wid, 2 * bs.y ) ){
          fill( p.col );
          rect( 7.875 * bs.x - ( p.no * wid ), 7 * bs.y, wid, 2 * bs.y );
          flag = true;
          
          switch( p.role ){
            case CONTINGENCY_PLANNER:   //危機管理官
              str = makeStringList( ts,"危機管理官","〇1アクションを消費して捨て札のイベントカードを1枚回収できます。" );
            break;
            case DISPATCHER:            //通信指令員
              str = makeStringList( ts,"通信指令員","〇1アクションを消費して他のプレイヤーを別のプレイヤーがいる都市に移動することができます。" );
            break;
            case OPERATIONS_EXPERT:     //作戦エキスパート
              str = makeStringList( ts,"作戦エキスパート","〇任意の都市カードを捨てて自分のいる都市に調査基地を設置することができます。","〇自分が調査基地にいる時、任意の都市カードを捨てて任意の都市に移動できます。" );
            break;
            case MEDIC:                 //衛生兵
              str = makeStringList( ts,"衛生兵","〇感染の治療を行う時に同じ色の病原体を全て取り除くことができます。","〇治療薬が作成されていて、移動先にその色の病原体がある時にアクションを消費しないで取り除くことができます。" );
            break;
            case SCIENTIST:             //科学者
              str = makeStringList( ts,"科学者","〇同じ色の都市カード4枚で治療薬を作成することができます。" );
            break;
            case RESEARCHER:            //研究員
              str = makeStringList( ts,"研究員","〇1アクションを消費して自分の手札の都市カード1枚を同じ都市にいるプレイヤーに渡すことができます。" );
            break;
            case QUARANTINE_SPECIALIST: //検疫官
              str = makeStringList( ts,"検疫官","〇自分がいる都市とその隣接の都市に病原体が置かれることを防ぎます。" );
            break;
          }//switch
        }//if
      }//for
      
      for( i = 0; i < playerActionImgSet.size(); i++ ){
        if( playerActionImgSet.get(i).isHit() ){
          playerActionImgSet.get(i).display();
          flag = true;
          
          switch( i ){
            case 0: //チャーター便による移動
              str = makeStringList( ts,"チャーター便による移動","〇プレイヤーが現在いる都市と同じ都市のカードを捨てることで任意の都市に移動することができます。" );
            break;
            case 1: //調査基地の設置
              str = makeStringList( ts,"調査基地の設置","〇プレイヤーが現在いる都市と同じ都市のカードを捨てることでその都市に調査基地を設置します。",
                "〇調査基地のある都市では治療薬を作成することができます。","〇調査基地のある都市同士で移動することができます。" );
            break;
            case 2: //知識の共有
              str = makeStringList( ts,"知識の共有","〇同じ都市にいるプレイヤーからその都市のカードを受け取ることができます。","〇同じ都市にいるプレイヤーにその都市のカードを渡すことができます。" );
            break;
            case 3: //治療薬の発見
              str = makeStringList( ts,"治療薬の発見","〇調査基地のある都市で、同色の都市カードを5枚(科学者は4枚)捨てることで、その色の治療薬を作成することができます。",
              "〇治療薬と同じ色の病原体は1度の感染の治療で全て取り除くことができます。");
            break;
            case 4: //イベントカード
              str = makeStringList( ts,"イベントカード","〇感染カード処理時以外、任意のタイミングでアクションを消費せずに発動ができるカードです。",
                "・空輸：任意のプレイヤーを任意の都市に移動します。",
                "・予測：感染カードのデッキの上から6枚のカードを見て、好きな順番でデッキに戻す。",
                "・政府の補助：任意の都市に調査基地を設置することができます。",
                "・静かな夜：発動ターンの感染カードのドローを行いません。",
                "・人口回復：感染カードの捨て札のカード1枚をゲームから取り除く。" );
            break;
            case 5: //特殊技能
              str = makeStringList( ts,"特殊技能","〇アクションを消費して発動する特別なスキルを持っている場合、使うことができます。" );
            break;
          }//switch
        }//if
      }//for

      for( i = 1; i < boardImgSet.size() - 1; i++ ){
        if( boardImgSet.get( i ).isHit() ){
          boardImgSet.get( i ).display();
          flag = true;
          
          switch( i ){
            case 1: //感染カード
              str = makeStringList( ts,"感染カード","〇使用済みのカードは黒色で表示されます。","〇除外されているカードは灰色で標示されます。" );
            break;
            case 2: //プレイヤーカード
              str = makeStringList( ts,"プレイヤーカード","〇使用済みのカードは黒色で表示されます。","〇除外されているカードは灰色で標示されます。" );
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
            case 0: colorName = "青";   break;
            case 1: colorName = "赤";   break;
            case 2: colorName = "青緑"; break;
            case 3: colorName = "紫";   break;
          }//switch
          str = makeStringList( ts,colorName + "色病原体の感染の治療",
            "〇プレイヤーが現在いる都市の" + colorName + "色病原体を1つ取り除くことができます",
            "〇" + colorName + "色の治療薬を作成すると、プレイヤーが現在いる都市の" + colorName + "色病原体を全て取り除くことができます。",
            "〇全ての都市から" + colorName + "色病原体が取り除かれると" + colorName + "色の都市の感染カードを引いても病原体が置かれることはありません。" );
        }//if
      }//for
      
      for( i = 0; i < 3; i++ ){
        if( rectHit( 0, i * bs.y , bs.x, bs.y ) ){
          flag = true;
          switch( i ){
            case 0: //アクションカウント
              str = makeStringList( ts,"アクション","〇実行可能な残りアクション数です。" );
              numbers.get( ACTION_COUNT ).display( 0, 0, bs.x, bs.y );
            break;
            case 1: //感染率
              str = makeStringList( ts,"感染率","〇ターン終了時にドローする感染カードの枚数です。","〇エピデミックカードをドローした時に感染率が進行します。" );
              numbers.get( gameStatus.infectionRateTrack[gameStatus.irtCnt] ).display( 0, bs.y, bs.x, bs.y );
            break;
            case 2: //アウトブレイク回数
              str = makeStringList( ts,"アウトブレイク","〇発生したアウトブレイクの数です。","〇8回アウトブレイクが起こると敗北します。" );
              numbers.get( gameStatus.outBreakCount ).display( 0, 2 * bs.y, bs.x, bs.y );
            break;
          }//switch
        }//if
      }//for
      
      //テキストの表示
      helpText( str, flag );
      
    }//if
  }//helpDraw
  
  void helpText( StringList str, boolean flag ){
    textSize( TEXT_SIZE * 5 );
    fill( BLACK );
    if( flag ){
      text( str.get( 0 ), width * 0.5, height * 0.125 );
      textAlign( LEFT, CENTER );
      textSize( TEXT_SIZE * 2.5 );
      for( int i = 1; i < str.size(); i++ ){ 
        text( str.get( i ), width * 0.125, height * 0.125 + i * TEXT_SIZE * 5 );
      }
    }else{
      text( str.get( 0 ), width * 0.5, height * 0.125 );
      textAlign( LEFT, CENTER );
      textSize( TEXT_SIZE * 2 );
      for( int i = 1; i < str.size(); i++ ){
        text( str.get( i ), width * 0.125, height * 0.125 + i * TEXT_SIZE * 2 );
      }
    }//if
    str.clear();
    textAlign( CENTER, CENTER );
  }//helpText
  
  void gameInputDraw(){
    int i,j,k;
    float ts = ( width + height ) / 18;
    float x = width * 0.25, y  = height * 0.375;
    
    background( DARKBLUE );
    for( i = 0; i < 3; i++ ){ 
      textSize( ts );
      fill( textColorPattern[i] );
      text( "PANDEMIC", width * 0.5 + ( i * 5 ), ts + ( i * 5 ) );
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
            text( "□プレイヤー" + ( j + 1 ) + "の名前: " + str, x, y + TEXT_SIZE * ( j + 2 ) + i * 2 );
          }
        case PLAYER_NUMBER_INPUT:
          text( "□プレイヤーの人数: " + playerNumber + " (2 ～ 4)", x, y + TEXT_SIZE + i * 2 );
        case GAME_DIFFICULTY_INPUT:
          text( "□エピデミックカードの枚数: " + epidemicCardNumber + " (4 ～ 6)", x, y + i * 2 );
        break;
        default:
      }
      textAlign( CENTER, CENTER );
    }
  }
  
  void gameOverDraw(){
    color col;
    float ts = TEXT_SIZE * 5;
    StringList str = new StringList();
    
    if( gameStatus.gameOverFlag ){
      col = YELLOW;
      str = makeStringList( ts,"勝利", "全ての病原体を排除した!" );
    }else{
      col = DARKBLUE;
      str = makeStringList( ts,"敗北", "人類は滅亡しました" );
    }
    fill( col, 200 );
    rect( 0, 0, width, height );
    
    textSize( ts );
    fill( BLACK );
    for( int i = 0; i < str.size(); i++ ){ 
      text( str.get( i ), width * 0.5, height * 0.25 + i * TEXT_SIZE * 5 );
    }
    str.clear();
  }//gameOverDraw
}