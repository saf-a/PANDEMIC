void specialSkillProcess(){
  player p = players.get( MAIN_TURN.n );
  switch( p.role ){
    case CONTINGENCY_PLANNER:
      if( draws.displayCard.size() <= 0 ){
        //イベントカードを表示する
        for( int i = 0; i < gameStatus.playerDiscardPile.size(); i++ ){
          int num = gameStatus.playerDiscardPile.get( i );
          if( AIRLIFT <= num && num <= RESILIENT_POPULATION ){
            draws.displayCard.append( num );
          }
        }//for
        //イベントカードが存在しないならリセット
        if( draws.displayCard.size() <= 0 ){
          gameStatus.resetSetting(); 
        }
      }else{
        //イベントカードを表示する
        draws.cardSelectDraw();
        //イベントカードを選択したら、そのカードを手札に加える
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
      if( searchList( gameStatus.researchStationList, players.get( MAIN_TURN.n ).position ) ){  //調査基地のある都市にいる時
        draws.cardSelectDraw();
        if( draws.selectCard.size() >= 1 ){
          gameStatus.charterFlightFlag = true;
          phase = PHASE.GAME;
        }
      }else{  //調査基地のない都市にいる時
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
        
        //srcのカードをdestにセットする
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

//イベントカードの処理
void eventCardProcess(){
  int i;
  
  if( draws.selectCard.size() <= 0 ){
    draws.cardSelectDraw();
  }else{
    switch( draws.selectCard.get( 0 ) ){
      case AIRLIFT:  //空輸
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
      case FORECAST:  //予測
        if( draws.displayCard.size() <= 0 ){
          //表示するカードの情報を入れる
          for( i = 1; i <= 6; i++ ){
            draws.displayCard.append( gameStatus.infectionDeck.get( gameStatus.infectionDeck.size() - i ) );
          }
          gameStatus.pressFlag = false;
        }else{
          draws.cardSelectDraw();
          if( draws.selectCard.size() >= 7 ){  //1枚目イベントカード + 6枚
            for( i = 1; i <= 6; i++ ){
              gameStatus.infectionDeck.set( gameStatus.infectionDeck.size() - i, draws.selectCard.get( i ) );
            }
            players.get( draws.targetPlayerNo ).removeCard( draws.selectCard.get( 0 ) );
            gameStatus.resetSetting();
          }
        }
      break;
      case GOVERNMENT_GRANT:  //政府の補助
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
      case ONE_QUIET_NIGHT:  //静かな夜
        gameStatus.oneQuietNightFlag = true;
        players.get( draws.targetPlayerNo ).removeCard( draws.selectCard.get( 0 ) );
        gameStatus.resetSetting();
      break;
      case RESILIENT_POPULATION:  //人口回復
        if( draws.displayCard.size() <= 0 ){
          for( i = 0; i < gameStatus.infectionDiscardPile.size(); i++ ){
            draws.displayCard.append( gameStatus.infectionDiscardPile.get( i ) );
          }
          draws.displayCard.sort();
        }
        draws.cardSelectDraw();
        if( draws.selectCard.size() >= 2 ){  //1枚目イベントカード + 選択した1枚
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

//知識の共有
void shareKnowledgeProcess(){
  draws.playerSelectDraw();
  if( draws.selectPlayerNo.size() >= 1 ){
    player src = players.get( phases.shareKnowledgeSrc );
    player dest = players.get( draws.selectPlayerNo.get( 0 ) );
    
    //srcのカードをdestにセットする
    dest.setCard( src.removeCard( src.position ) );
    
    ACTION_COUNT--;
    gameStatus.resetSetting();
  }
}//shareKnowledgeProcess

void handLimitProcess(){
  draws.cardSelectDraw();
  if( draws.selectCard.size() >= 1 ){
    player p = players.get( draws.targetPlayerNo );
    p.removeCard( draws.selectCard.get( 0 ) );
    
    gameStatus.resetSetting();
  }
}//handLimitProcess

void discoverCureProcess(){
  draws.cardSelectDraw();
  if( draws.selectCard.size() >= gameStatus.discoverCureCnt ){  //5枚のカードを選択した時、roleがSCIENTISTなら4枚
    int i;
    player p = players.get( draws.targetPlayerNo );
    //最初のカラーをcolに
    int col = todoufuken.get( draws.selectCard.get( 0 ) ).col;
    //1つ目とその他を比べていく
    for( i = 1; i < draws.selectCard.size(); i++ ){
      //違ったらブレイク
      if( col != todoufuken.get( draws.selectCard.get( i ) ).col ){
        break;
      }
    }
    //最後までループ(=色が同じ)したら治療薬を作成
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

GAME_OVER gameOverCheck(){
  int i, pathogenTotalCnt = 0, cnt = 0;
  boolean flag = false;
  
  //勝利
  for( i = 0; i < gameStatus.cureMarkersFlag.length; i++ ){
    if( gameStatus.cureMarkersFlag[i] >= 1 ){
      cnt++;
    }
  }
  //全ての治療薬を作成した時、勝利
  if( cnt >= gameStatus.cureMarkersFlag.length ){
    gameStatus.gameOverFlag = true;
    return GAME_OVER.WIN;
  }
  
  ////敗北
  //置かれている各色の病原体の数を合計する
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
  //パンデミック回数が8以上 か 置かれている各色の病原体数が24以上 か プレイヤーデッキの数が0以下になった時、敗北
  if( gameStatus.outBreakCount >= 8 || flag || gameStatus.playerDeck.size() <= 0 ){
    gameStatus.gameOverFlag = false;
    return GAME_OVER.LOSE;
  }
  
  //他
  return GAME_OVER.CONTINUE;
}//gameOverCheck

void turnChange(){
  int i, card;
  player p = players.get( MAIN_TURN.n );
  
  //プレイヤーにカードをセット
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
  
  //感染カードをドロー
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
  
  //次のプレイヤーにターンをチェンジ
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

//rect型のHit用
boolean rectHit( float x, float y, float w, float h ){
  return ( x < mouseX && mouseX < x + w  && y < mouseY && mouseY < y + h );
}//rectHit

//エピデミックカードを引いた時の処理
void epidemicCardProcess(){
  //感染率の上昇
  gameStatus.irtCnt++;
  
  //感染
  int card = removeAndAppendList( gameStatus.infectionDiscardPile, gameStatus.infectionDeck, 0 );
  gameStatus.epidemicCardName = todoufuken.get( card ).name;
  addPathogenProcess( card, todoufuken.get( card ).col, 3 );
  gameStatus.outbreakLoopList.clear();
  
  //度合いの増加
  gameStatus.infectionDiscardPile.shuffle();
  for( int i = 0; i < gameStatus.infectionDiscardPile.size(); i++ ){
    gameStatus.infectionDeck.append( gameStatus.infectionDiscardPile.get( i ) );
  }
  gameStatus.infectionDiscardPile.clear();
  
  draws.epidemicCardDrawFrame = frameRate * 2;
}//epidemicCardProcess

//病原体を都市にsetNum個追加する
//setNumがCUREの時に0にする
void addPathogenProcess( int position, int colorsNumber, int setNum ){
  prefecture t = todoufuken.get( position );
  
  //根絶されている場合はreturn
  if( gameStatus.cureMarkersFlag[colorsNumber] == 2 ){
    return;
  }
  
  if( gameStatus.outbreakLoopList.hasValue( position ) == false && quarantineSpecialistHit( position, setNum ) ){
    if( t.pathogenCnt[colorsNumber] == 0 && setNum <= -1 ){
      ACTION_COUNT++;
      return;
    }
    
    if( setNum == CURE ){  //CUREの時0
      t.pathogenCnt[colorsNumber] = 0;
      return;
    }
    
    if( t.pathogenCnt[colorsNumber] + setNum > 3 ){  //アウトブレイク処理
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

//役職が検疫官の時の判定
boolean quarantineSpecialistHit( int position, int cnt ){
  int pNo = -1;
  
  //置く個数が-1以下(=取り除く)かCUREの時
  if( cnt <= -1 ){
    return true; 
  }
  
  //プレイヤーの中に検疫官がいるかどうか
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

void setupVar(){
  int i;
  float f;
  String[] names = new String[52];
  float[] xZahyou = new float[52], yZahyou = new float[52];
  int[] col = new int[52];
  
  bs = new PVector( width / 9, height / 9, 0 );  //分割ブロックのサイズ
  ELLIPSE_SIZE  = ( width + height ) / 100;
  RECT_SIZE = ( width + height ) / 30;
  TEXT_SIZE = ( width + height ) / 60;  //一時的
  
  //北海道地方
  names[0]  = "北海道"; xZahyou[0]  = width/1.23;  yZahyou[0]  = height/5.95;  col[0]  = 0;
  //東北地方
  names[1]  = "青森";   xZahyou[1]  = width/1.41;  yZahyou[1]  = height/2.84;  col[1]  = 0;
  names[2]  = "岩手";   xZahyou[2]  = width/1.36;  yZahyou[2]  = height/2.38;  col[2]  = 0;
  names[3]  = "宮城";   xZahyou[3]  = width/1.40;  yZahyou[3]  = height/2.05;  col[3]  = 0;
  names[4]  = "秋田";   xZahyou[4]  = width/1.46;  yZahyou[4]  = height/2.41;  col[4]  = 0;
  names[5]  = "山形";   xZahyou[5]  = width/1.49;  yZahyou[5]  = height/2.02;  col[5]  = 0;
  names[6]  = "福島";   xZahyou[6]  = width/1.48;  yZahyou[6]  = height/1.78;  col[6]  = 0;
  
  //関東地方
  names[7]  = "茨城";   xZahyou[7]  = width/1.46;  yZahyou[7]  = height/1.57;  col[7]  = 0;
  names[8]  = "栃木";   xZahyou[8]  = width/1.52;  yZahyou[8]  = height/1.66;  col[8]  = 0;
  names[9]  = "群馬";   xZahyou[9]  = width/1.63;  yZahyou[9]  = height/1.62;  col[9]  = 0;
  names[10] = "埼玉";   xZahyou[10] = width/1.60;  yZahyou[10] = height/1.54;  col[10] = 1;
  names[11] = "千葉";   xZahyou[11] = width/1.46;  yZahyou[11] = height/1.47;  col[11] = 1;
  names[12] = "東京";   xZahyou[12] = width/1.55;  yZahyou[12] = height/1.50;  col[12] = 1;
  names[13] = "神奈川"; xZahyou[13] = width/1.59;  yZahyou[13] = height/1.45;  col[13] = 1;
  //中部地方
  names[14] = "新潟";   xZahyou[14] = width/1.64;  yZahyou[14] = height/1.79;  col[14] = 0;
  names[15] = "富山";   xZahyou[15] = width/1.92;  yZahyou[15] = height/1.64;  col[15] = 1;
  names[16] = "石川";   xZahyou[16] = width/2.00;  yZahyou[16] = height/1.73;  col[16] = 1;
  names[17] = "福井";   xZahyou[17] = width/2.14;  yZahyou[17] = height/1.53;  col[17] = 1;
  names[18] = "山梨";   xZahyou[18] = width/1.70;  yZahyou[18] = height/1.49;  col[18] = 1;
  names[19] = "長野";   xZahyou[19] = width/1.79;  yZahyou[19] = height/1.56;  col[19] = 1;
  names[20] = "岐阜";   xZahyou[20] = width/1.97;  yZahyou[20] = height/1.51;  col[20] = 1;
  names[21] = "静岡";   xZahyou[21] = width/1.79;  yZahyou[21] = height/1.40;  col[21] = 1;
  names[22] = "愛知";   xZahyou[22] = width/1.94;  yZahyou[22] = height/1.40;  col[22] = 1;
  
  //近畿地方
  names[23] = "三重";   xZahyou[23] = width/2.12;  yZahyou[23] = height/1.36;  col[23] = 2;
  names[24] = "滋賀";   xZahyou[24] = width/2.19;  yZahyou[24] = height/1.44;  col[24] = 2;
  names[25] = "京都";   xZahyou[25] = width/2.40;  yZahyou[25] = height/1.44;  col[25] = 2;
  names[26] = "大阪";   xZahyou[26] = width/2.35;  yZahyou[26] = height/1.36;  col[26] = 2;
  names[27] = "兵庫";   xZahyou[27] = width/2.61;  yZahyou[27] = height/1.43;  col[27] = 2;
  names[28] = "奈良";   xZahyou[28] = width/2.25;  yZahyou[28] = height/1.33;  col[28] = 2;
  names[29] = "和歌山"; xZahyou[29] = width/2.40;  yZahyou[29] = height/1.29;  col[29] = 2;
  //中国地方
  names[30] = "鳥取";   xZahyou[30] = width/2.96;  yZahyou[30] = height/1.45;  col[30] = 2;
  names[31] = "島根";   xZahyou[31] = width/3.84;  yZahyou[31] = height/1.41;  col[31] = 2;
  names[32] = "岡山";   xZahyou[32] = width/3.02;  yZahyou[32] = height/1.38;  col[32] = 2;
  names[33] = "広島";   xZahyou[33] = width/3.58;  yZahyou[33] = height/1.36;  col[33] = 2;
  names[34] = "山口";   xZahyou[34] = width/4.83;  yZahyou[34] = height/1.32;  col[34] = 2;
  
  //四国地方
  names[35] = "徳島";   xZahyou[35] = width/2.81;  yZahyou[35] = height/1.28;  col[35] = 3;
  names[36] = "香川";   xZahyou[36] = width/2.92;  yZahyou[36] = height/1.32;  col[36] = 3;
  names[37] = "愛媛";   xZahyou[37] = width/3.54;  yZahyou[37] = height/1.26;  col[37] = 3;
  names[38] = "高知";   xZahyou[38] = width/3.28;  yZahyou[38] = height/1.24;  col[38] = 3;
  //九州・沖縄地方
  names[39] = "福岡";   xZahyou[39] = width/5.99;  yZahyou[39] = height/1.26;  col[39] = 3;
  names[40] = "佐賀";   xZahyou[40] = width/7.53;  yZahyou[40] = height/1.23;  col[40] = 3;
  names[41] = "長崎";   xZahyou[41] = width/8.13;  yZahyou[41] = height/1.19;  col[41] = 3;
  names[42] = "熊本";   xZahyou[42] = width/5.95;  yZahyou[42] = height/1.17;  col[42] = 3;
  names[43] = "大分";   xZahyou[43] = width/5.02;  yZahyou[43] = height/1.22;  col[43] = 3;
  names[44] = "宮崎";   xZahyou[44] = width/5.09;  yZahyou[44] = height/1.14;  col[44] = 3;
  names[45] = "鹿児島"; xZahyou[45] = width/6.56;  yZahyou[45] = height/1.11;  col[45] = 3;
  names[46] = "沖縄";   xZahyou[46] = width/24.98; yZahyou[46] = height/1.04;  col[46] = 3;
  
  //イベントカード
  names[47] = "空輸";       xZahyou[47] = -100; yZahyou[47] = -100;  col[47] = 4;
  names[48] = "予測";       xZahyou[48] = -100; yZahyou[48] = -100;  col[48] = 4;
  names[49] = "政府の補助"; xZahyou[49] = -100; yZahyou[49] = -100;  col[49] = 4;
  names[50] = "静かな夜";   xZahyou[50] = -100; yZahyou[50] = -100;  col[50] = 4;
  names[51] = "人口回復";   xZahyou[51] = -100; yZahyou[51] = -100;  col[51] = 4;
  
  //都道府県の設定
  todoufuken.clear();
  for( i = 0; i < todoufukenNum + eventCardNum; i++ ){
    todoufuken.add( new prefecture( names[i], i, xZahyou[i], yZahyou[i], wallSet(i), col[i], 0, adjacentSet(i), false ) );
  }
  todoufuken.get( 12 ).setResearchStation();  //東京に調査基地を設定
  
  ////画像の読み込み
  boardImgSet.clear();
  playerActionImgSet.clear();
  TreatDiseases.clear();
  cureMarkers.clear();
  numbers.clear();
  jouge.clear();
  //0:ボード 1:感染カード 2:プレイヤーカード 3:調査基地
  String[] nameBo = {"image/nihon.png","image/biohazard.png","image/redCross.png","image/researchStations.png"};
  float[] x ={ 0     , bs.x      , bs.x * 2   , bs.x * 8.5 };
  float[] y ={ 0     , 0         , 0          , bs.y * 3.5 };
  float[] w ={ width , bs.x      , bs.x * 1.75, bs.x * 0.5 };
  float[] h ={ height, bs.y * 4.5, bs.y * 4.5 , bs.y * 0.5 };
  for( i = 0; i < nameBo.length; i++ ){
    boardImgSet.add( new ImageSet( nameBo[i], x[i], y[i], w[i], h[i] ) );
  }
  
  //0:チャーター便による移動　1:調査基地の設置 2:治療薬の発見 3:知識の共有 4:イベントカード 5:特殊技能
  String[] namePa = {"image/CharterFlight.png","image/BuildResearchStation.png","image/ShareKnowledge.png","image/DiscoverCure.png","image/eventCard.png","image/specialSkill.png"};
  for( i = 0, f = 0; i < namePa.length - 1; i++ ){
    playerActionImgSet.add( new ImageSet( namePa[i], 8.5 * bs.x, ( 4.0 + f ) * bs.y, 0.5 * bs.x, 0.5 * bs.y ) );
    f+=0.5;
  }
  playerActionImgSet.add( new ImageSet( namePa[i], 8.0 * bs.x, ( 3.5 + f ) * bs.y, 0.5 * bs.x, 0.5 * bs.y ) );
  
  //治療 0:青 1:赤 2:黄 3:紫
  String[] nameTd = {"image/TreatDiseaseBlue.png","image/TreatDiseaseRed.png","image/TreatDiseaseLightseagreen.png","image/TreatDiseasePurple.png"};
  for( i = 0, f = 0; i < nameTd.length; i++ ){
    TreatDiseases.add( new ImageSet( nameTd[i], 8.0 * bs.x, ( 4.0 + f ) * bs.y, 0.5 * bs.x, 0.5 * bs.y ) );
    f+=0.5;
  }
  
  //治療薬 0:青 1:赤 2:黄 3:紫
  String[] nameCm = {"image/CureMarkerRBlue.png","image/CureMarkerRed.png","image/CureMarkerLightseagreen.png","image/CureMarkerPurple.png"};
  for( i = 0, f = 0; i < nameCm.length; i++ ){
    cureMarkers.add( new ImageSet( nameCm[i], 8.0 * bs.x, ( 4.0 + f ) * bs.y, 0.5 * bs.x, 0.5 * bs.y ) );
    f+=0.5;
  }
  
  //数字
  for( i = 0; i < 9; i++ ){
    numbers.add( new ImageSet( "image/" + i + ".png" ) );
  }
  
  //上下
  String[] nameJouge = {"image/ue.png","image/sita.png"};
  for( i = 0; i < 2; i++ ){
    jouge.add( new ImageSet( nameJouge[i], 3 * width / 4, height / 4 + i * ( 0.5 * bs.y ), 0.5 * bs.x, 0.5 * bs.y ) );
  }  
  
  //感染カードの設定
  gameStatus.infectionDeck = setList( todoufukenNum );
  
  //ヘルプ用のテキスト
  rule = txtLoad( rule, "txt/rule.txt" );
  
  //判定用
  gameStatus.hitLine.clear();
  gameStatus.hitLine.add( new PVector( mouseX, mouseY ) ); //上
  gameStatus.hitLine.add( new PVector( mouseX, mouseY ) ); //下
  gameStatus.hitLine.add( new PVector( mouseX, mouseY ) ); //左
  gameStatus.hitLine.add( new PVector( mouseX, mouseY ) ); //右
  
  //フェーズを移行
  phase = PHASE.GAME_DIFFICULTY_INPUT;
}//setupVar

//テキストをロードする
String[] txtLoad( String[] txt, String path ){
  txt = loadStrings( path );
  txt[0] = txt[0].substring( 1, txt[0].length() );
  return txt;
}

//Listからremoveする値をappendするListに入れ、その値を返却
int removeAndAppendList( IntList apnd, IntList rmv, int rmvNum ){
  int card = rmv.remove( rmvNum );
  apnd.append( card );
  
  return card;
}

//Listに指定する範囲のランダムな数字をセットする
IntList setList( int num ){
  IntList lst = new IntList();
  for( int i = 0; i < num; i++ ){
    lst.append(i);
  }
  lst.shuffle();
  
  return lst;
}//setList

//Listに対象の数字があった場合、trueを返却
//Listに対象の数字がない場合、falseを返却
boolean searchList( IntList lst ,int... num ){
  
  for( int i = 0; i < num.length; i++ ){
    if( lst.hasValue( num[i] ) == true ) {
      return true;
    }
  }
  return false;
  
}//searchList

//引数の配列からIntList型を作成
IntList initList( int... num ){
  IntList lst = new IntList();
  for( int i = 0; i < num.length; i++ ){
    lst.append( num[i] );
  }
  return lst;
}

//Sting型の配列からStingList型を作成
StringList makeStringList( float txtSize, String... strs ){
  int i, j;
  int loopNum, amari, perlineNum = floor( ( width * 0.75 ) / txtSize );
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
      ret.append( "　" + strs[i].substring( st, sp ) );
    }
  }
  
  return ret;
}

boolean pressButton(){
  if( gameStatus.pressFlag && mousePressed ){
    gameStatus.pressFlag = false;
    return true;
  }else{
    return false;
  }
}

void getList( IntList src, IntList dest ){
  for( int i = 0; i < src.size(); i++ ){
    dest.append( src.get( i ) );
  }
}

//隣接都道府県の設定
IntList adjacentSet( int i ){
  switch( i ){
    case 0:  //北海道
      return initList(1);
    case 1:  //青森
      return initList(0,2,4);
    case 2:  //岩手
      return initList(1,3,4);
    case 3:  //宮城
      return initList(2,4,5,6);
    case 4:  //秋田
      return initList(1,2,3,5);
    case 5:  //山形
      return initList(3,4,6,14);
    case 6:  //福島
      return initList(3,5,7,8,9,14);
    case 7:  //茨城
      return initList(6,8,10,11);
    case 8:  //栃木
      return initList(6,7,9,10);
    case 9:  //埼玉
      return initList(6,8,10,14,19);
    case 10:  //埼玉
      return initList(7,8,9,11,12,18,19);
    case 11:  //千葉
      return initList(7,10,12);
    case 12:  //東京
      return initList(10,11,13,18);
    case 13:  //神奈川
      return initList(12,18,21);
    case 14:  //新潟
      return initList(5,6,9,15,19);
    case 15:  //富山
      return initList(14,16,19,20);
    case 16:  //石川
      return initList(15,17,20);
    case 17:  //福井
      return initList(16,20,24,25);
    case 18:  //山梨
      return initList(10,12,13,19,21);    
    case 19:  //長野
      return initList(9,10,14,15,18,20,21,22);
    case 20:  //岐阜
      return initList(15,16,17,19,22,23,24);    
    case 21:  //静岡
      return initList(13,18,19,22);    
    case 22:  //愛知
      return initList(19,20,21,23);
    case 23:  //三重
      return initList(20,22,24,25,28,29);
    case 24:  //滋賀
      return initList(17,20,23,25);
    case 25:  //京都
      return initList(17,23,24,26,27,28);
    case 26:  //大阪
      return initList(25,27,28,29);
    case 27:  //兵庫
      return initList(25,26,30,32);
    case 28:  //奈良
      return initList(23,25,26,29);
    case 29:  //和歌山
      return initList(23,26,28);
    case 30:  //鳥取
      return initList(27,31,32,33);
    case 31:  //島根
      return initList(30,33,34);
    case 32:  //岡山
      return initList(27,30,33,36);
    case 33:  //広島
      return initList(30,31,32,34,37);
    case 34:  //山口
      return initList(31,33,39);
    case 35:  //徳島
      return initList(36,37,38);
    case 36:  //香川
      return initList(32,35,37);
    case 37:  //愛媛
      return initList(33,35,36,38,43);
    case 38:  //高知
      return initList(35,37);
    case 39:  //福岡
      return initList(34,40,42,43);
    case 40:  //佐賀
      return initList(39,41);
    case 41:  //長崎
      return initList(40,46);
    case 42:  //熊本
      return initList(39,43,44,45);
    case 43:  //大分
      return initList(37,39,42,44);
    case 44:  //宮崎
      return initList(42,43,45);
    case 45:  //鹿児島
      return initList(42,44,46);
    case 46:  //沖縄
      return initList(41,45);
    default:
      return initList(-1);
  }
}

ArrayList<PVector> wallSet( int i ){
  ArrayList<PVector> wall = new ArrayList<PVector>();
  wall.clear();
  switch( i ){
    case 0:  //北海道
      wall.add( new PVector( width / 1.48, height / 3.29 ) );
      wall.add( new PVector( width / 1.50, height / 3.45 ) );
      wall.add( new PVector( width / 1.48, height / 3.66 ) );
      wall.add( new PVector( width / 1.48, height / 3.79 ) );
      wall.add( new PVector( width / 1.52, height / 4.04 ) );
      wall.add( new PVector( width / 1.52, height / 4.19 ) );
      wall.add( new PVector( width / 1.52, height / 4.54 ) );
      wall.add( new PVector( width / 1.44, height / 5.19 ) );
      wall.add( new PVector( width / 1.46, height / 5.45 ) );
      wall.add( new PVector( width / 1.45, height / 5.74 ) );
      wall.add( new PVector( width / 1.44, height / 5.90 ) );
      wall.add( new PVector( width / 1.40, height / 5.45 ) );
      wall.add( new PVector( width / 1.39, height / 5.65 ) );
      wall.add( new PVector( width / 1.37, height / 5.45 ) );
      wall.add( new PVector( width / 1.34, height / 5.97 ) );
      wall.add( new PVector( width / 1.35, height / 6.97 ) );
      wall.add( new PVector( width / 1.32, height / 7.66 ) );
      wall.add( new PVector( width / 1.32, height / 9.56 ) );
      wall.add( new PVector( width / 1.31, height / 11.61 ) );
      wall.add( new PVector( width / 1.31, height / 16.36 ) );
      wall.add( new PVector( width / 1.33, height / 23.48 ) );
      wall.add( new PVector( width / 1.33, height / 38.57 ) );
      wall.add( new PVector( width / 1.30, height / 60.00 ) );
      wall.add( new PVector( width / 1.19, height / 10.69 ) );
      wall.add( new PVector( width / 1.12, height / 8.44 ) );
      wall.add( new PVector( width / 1.08, height / 7.66 ) );
      wall.add( new PVector( width / 1.05, height / 9.56 ) );
      wall.add( new PVector( width / 1.04, height / 9.15 ) );
      wall.add( new PVector( width / 1.06, height / 7.06 ) );
      wall.add( new PVector( width / 1.05, height / 6.35 ) );
      wall.add( new PVector( width / 1.04, height / 5.74 ) );
      wall.add( new PVector( width / 1.02, height / 6.00 ) );
      wall.add( new PVector( width / 1.04, height / 5.40 ) );
      wall.add( new PVector( width / 1.06, height / 5.37 ) );
      wall.add( new PVector( width / 1.06, height / 5.14 ) );
      wall.add( new PVector( width / 1.08, height / 5.27 ) );
      wall.add( new PVector( width / 1.08, height / 5.07 ) );
      wall.add( new PVector( width / 1.12, height / 5.07 ) );
      wall.add( new PVector( width / 1.18, height / 4.27 ) );
      wall.add( new PVector( width / 1.18, height / 3.72 ) );
      wall.add( new PVector( width / 1.32, height / 4.54 ) );
      wall.add( new PVector( width / 1.39, height / 4.19 ) );
      wall.add( new PVector( width / 1.41, height / 4.48 ) );
      wall.add( new PVector( width / 1.45, height / 4.44 ) );
      wall.add( new PVector( width / 1.47, height / 4.11 ) );
      wall.add( new PVector( width / 1.44, height / 3.91 ) );
      wall.add( new PVector( width / 1.41, height / 3.96 ) );
      wall.add( new PVector( width / 1.40, height / 3.91 ) );
      wall.add( new PVector( width / 1.40, height / 3.75 ) );
      wall.add( new PVector( width / 1.37, height / 3.66 ) );
      wall.add( new PVector( width / 1.39, height / 3.56 ) );
      wall.add( new PVector( width / 1.41, height / 3.65 ) );
      wall.add( new PVector( width / 1.43, height / 3.56 ) );
      wall.add( new PVector( width / 1.45, height / 3.56 ) );
      wall.add( new PVector( width / 1.45, height / 3.43 ) );
      wall.add( new PVector( width / 1.48, height / 3.29 ) );
      break;
    case 1:  //青森
      wall.add( new PVector( width / 1.50, height / 2.73 ) );
      wall.add( new PVector( width / 1.50, height / 2.82 ) );
      wall.add( new PVector( width / 1.50, height / 2.90 ) );
      wall.add( new PVector( width / 1.47, height / 2.91 ) );
      wall.add( new PVector( width / 1.46, height / 3.20 ) );
      wall.add( new PVector( width / 1.43, height / 3.20 ) );
      wall.add( new PVector( width / 1.42, height / 2.96 ) );
      wall.add( new PVector( width / 1.40, height / 2.96 ) );
      wall.add( new PVector( width / 1.40, height / 3.02 ) );
      wall.add( new PVector( width / 1.37, height / 2.96 ) );
      wall.add( new PVector( width / 1.36, height / 3.10 ) );
      wall.add( new PVector( width / 1.37, height / 3.15 ) );
      wall.add( new PVector( width / 1.41, height / 3.12 ) );
      wall.add( new PVector( width / 1.40, height / 3.36 ) );
      wall.add( new PVector( width / 1.36, height / 3.27 ) );
      wall.add( new PVector( width / 1.34, height / 3.29 ) );
      wall.add( new PVector( width / 1.35, height / 3.10 ) );
      wall.add( new PVector( width / 1.34, height / 2.81 ) );
      wall.add( new PVector( width / 1.32, height / 2.73 ) );
      wall.add( new PVector( width / 1.39, height / 2.66 ) );
      wall.add( new PVector( width / 1.38, height / 2.70 ) );
      wall.add( new PVector( width / 1.40, height / 2.76 ) );
      wall.add( new PVector( width / 1.42, height / 2.71 ) );
      wall.add( new PVector( width / 1.45, height / 2.75 ) );
      wall.add( new PVector( width / 1.50, height / 2.73 ) );
      break;
    case 2:  //岩手
      wall.add( new PVector( width / 1.39, height / 2.65 ) );
      wall.add( new PVector( width / 1.32, height / 2.71 ) );
      wall.add( new PVector( width / 1.30, height / 2.51 ) );
      wall.add( new PVector( width / 1.29, height / 2.39 ) );
      wall.add( new PVector( width / 1.28, height / 2.38 ) );
      wall.add( new PVector( width / 1.28, height / 2.32 ) );
      wall.add( new PVector( width / 1.29, height / 2.32 ) );
      wall.add( new PVector( width / 1.29, height / 2.23 ) );
      wall.add( new PVector( width / 1.32, height / 2.16 ) );
      wall.add( new PVector( width / 1.33, height / 2.17 ) );
      wall.add( new PVector( width / 1.340, height / 2.164 ) );
      wall.add( new PVector( width / 1.37, height / 2.10 ) );
      wall.add( new PVector( width / 1.37, height / 2.14 ) );
      wall.add( new PVector( width / 1.41, height / 2.16 ) );
      wall.add( new PVector( width / 1.42, height / 2.27 ) );
      wall.add( new PVector( width / 1.41, height / 2.35 ) );
      wall.add( new PVector( width / 1.41, height / 2.43 ) );
      wall.add( new PVector( width / 1.40, height / 2.62 ) );
      wall.add( new PVector( width / 1.39, height / 2.65 ) );
      break;
    case 3:  //宮城
      wall.add( new PVector( width / 1.43, height / 2.12 ) );
      wall.add( new PVector( width / 1.41, height / 2.15 ) );
      wall.add( new PVector( width / 1.37, height / 2.14 ) );
      wall.add( new PVector( width / 1.37, height / 2.11 ) );
      wall.add( new PVector( width / 1.34, height / 2.16 ) );
      wall.add( new PVector( width / 1.32, height / 2.15 ) );
      wall.add( new PVector( width / 1.34, height / 2.07 ) );
      wall.add( new PVector( width / 1.34, height / 1.98 ) );
      wall.add( new PVector( width / 1.35, height / 2.00 ) );
      wall.add( new PVector( width / 1.39, height / 1.95 ) );
      wall.add( new PVector( width / 1.39, height / 1.89 ) );
      wall.add( new PVector( width / 1.41, height / 1.86 ) );
      wall.add( new PVector( width / 1.46, height / 1.90 ) );
      wall.add( new PVector( width / 1.43, height / 1.99 ) );
      wall.add( new PVector( width / 1.44, height / 2.07 ) );
      wall.add( new PVector( width / 1.43, height / 2.07 ) );
      wall.add( new PVector( width / 1.43, height / 2.12 ) );
      wall.add( new PVector( width / 1.43, height / 2.12 ) );
      break;
    case 4:  //秋田
      wall.add( new PVector( width / 1.53, height / 2.51 ) );
      wall.add( new PVector( width / 1.51, height / 2.56 ) );
      wall.add( new PVector( width / 1.50, height / 2.70 ) );
      wall.add( new PVector( width / 1.45, height / 2.73 ) );
      wall.add( new PVector( width / 1.43, height / 2.70 ) );
      wall.add( new PVector( width / 1.40, height / 2.74 ) );
      wall.add( new PVector( width / 1.39, height / 2.72 ) );
      wall.add( new PVector( width / 1.39, height / 2.68 ) );
      wall.add( new PVector( width / 1.384, height / 2.693 ) );
      wall.add( new PVector( width / 1.405, height / 2.628 ) );
      wall.add( new PVector( width / 1.41, height / 2.43 ) );
      wall.add( new PVector( width / 1.41, height / 2.38 ) );
      wall.add( new PVector( width / 1.42, height / 2.30 ) );
      wall.add( new PVector( width / 1.42, height / 2.22 ) );
      wall.add( new PVector( width / 1.41, height / 2.18 ) );
      wall.add( new PVector( width / 1.43, height / 2.12 ) );
      wall.add( new PVector( width / 1.44, height / 2.15 ) );
      wall.add( new PVector( width / 1.46, height / 2.17 ) );
      wall.add( new PVector( width / 1.51, height / 2.20 ) );
      wall.add( new PVector( width / 1.49, height / 2.42 ) );
      wall.add( new PVector( width / 1.50, height / 2.47 ) );
      wall.add( new PVector( width / 1.53, height / 2.46 ) );
      wall.add( new PVector( width / 1.53, height / 2.51 ) );
      break;
    case 5:  //山形
      wall.add( new PVector( width / 1.51, height / 2.20 ) );
      wall.add( new PVector( width / 1.44, height / 2.15 ) );
      wall.add( new PVector( width / 1.43, height / 2.09 ) );
      wall.add( new PVector( width / 1.44, height / 2.07 ) );
      wall.add( new PVector( width / 1.43, height / 2.00 ) );
      wall.add( new PVector( width / 1.45, height / 1.93 ) );
      wall.add( new PVector( width / 1.47, height / 1.91 ) );
      wall.add( new PVector( width / 1.47, height / 1.86 ) );
      wall.add( new PVector( width / 1.53, height / 1.86 ) );
      wall.add( new PVector( width / 1.55, height / 1.88 ) );
      wall.add( new PVector( width / 1.55, height / 1.90 ) );
      wall.add( new PVector( width / 1.55, height / 1.93 ) );
      wall.add( new PVector( width / 1.52, height / 1.96 ) );
      wall.add( new PVector( width / 1.56, height / 2.03 ) );
      wall.add( new PVector( width / 1.51, height / 2.20 ) );
      break;
    case 6:  //福島
      wall.add( new PVector( width / 1.39, height / 1.88 ) );
      wall.add( new PVector( width / 1.38, height / 1.79 ) );
      wall.add( new PVector( width / 1.39, height / 1.69 ) );
      wall.add( new PVector( width / 1.41, height / 1.68 ) );
      wall.add( new PVector( width / 1.44, height / 1.69 ) );
      wall.add( new PVector( width / 1.44, height / 1.66 ) );
      wall.add( new PVector( width / 1.47, height / 1.68 ) );
      wall.add( new PVector( width / 1.50, height / 1.72 ) );
      wall.add( new PVector( width / 1.56, height / 1.69 ) );
      wall.add( new PVector( width / 1.60, height / 1.69 ) );
      wall.add( new PVector( width / 1.60, height / 1.79 ) );
      wall.add( new PVector( width / 1.55, height / 1.81 ) );
      wall.add( new PVector( width / 1.55, height / 1.85 ) );
      wall.add( new PVector( width / 1.52, height / 1.86 ) );
      wall.add( new PVector( width / 1.47, height / 1.85 ) );
      wall.add( new PVector( width / 1.47, height / 1.90 ) );
      wall.add( new PVector( width / 1.44, height / 1.89 ) );
      wall.add( new PVector( width / 1.41, height / 1.85 ) );
      wall.add( new PVector( width / 1.39, height / 1.88 ) );
      break;
    case 7:  //茨城
      wall.add( new PVector( width / 1.47, height / 1.69 ) );
      wall.add( new PVector( width / 1.44, height / 1.66 ) );
      wall.add( new PVector( width / 1.44, height / 1.69 ) );
      wall.add( new PVector( width / 1.41, height / 1.67 ) );
      wall.add( new PVector( width / 1.43, height / 1.57 ) );
      wall.add( new PVector( width / 1.42, height / 1.52 ) );
      wall.add( new PVector( width / 1.44, height / 1.53 ) );
      wall.add( new PVector( width / 1.47, height / 1.52 ) );
      wall.add( new PVector( width / 1.51, height / 1.52 ) );
      wall.add( new PVector( width / 1.54, height / 1.56 ) );
      wall.add( new PVector( width / 1.51, height / 1.59 ) );
      wall.add( new PVector( width / 1.47, height / 1.60 ) );
      wall.add( new PVector( width / 1.47, height / 1.69 ) );
      break;
    case 8:  //栃木
      wall.add( new PVector( width / 1.58, height / 1.68 ) );
      wall.add( new PVector( width / 1.51, height / 1.72 ) );
      wall.add( new PVector( width / 1.49, height / 1.71 ) );
      wall.add( new PVector( width / 1.47, height / 1.68 ) );
      wall.add( new PVector( width / 1.47, height / 1.61 ) );
      wall.add( new PVector( width / 1.51, height / 1.59 ) );
      wall.add( new PVector( width / 1.54, height / 1.57 ) );
      wall.add( new PVector( width / 1.55, height / 1.58 ) );
      wall.add( new PVector( width / 1.58, height / 1.59 ) );
      wall.add( new PVector( width / 1.57, height / 1.63 ) );
      wall.add( new PVector( width / 1.59, height / 1.64 ) );
      wall.add( new PVector( width / 1.58, height / 1.68 ) );
      break;
    case 9:  //群馬
      wall.add( new PVector( width / 1.63, height / 1.70 ) );
      wall.add( new PVector( width / 1.58, height / 1.69 ) );
      wall.add( new PVector( width / 1.59, height / 1.64 ) );
      wall.add( new PVector( width / 1.57, height / 1.63 ) );
      wall.add( new PVector( width / 1.58, height / 1.59 ) );
      wall.add( new PVector( width / 1.55, height / 1.58 ) );
      wall.add( new PVector( width / 1.55, height / 1.57 ) );
      wall.add( new PVector( width / 1.61, height / 1.57 ) );
      wall.add( new PVector( width / 1.67, height / 1.54 ) );
      wall.add( new PVector( width / 1.69, height / 1.56 ) );
      wall.add( new PVector( width / 1.68, height / 1.59 ) );
      wall.add( new PVector( width / 1.73, height / 1.61 ) );
      wall.add( new PVector( width / 1.71, height / 1.64 ) );
      wall.add( new PVector( width / 1.67, height / 1.65 ) );
      wall.add( new PVector( width / 1.63, height / 1.70 ) );
      break;
    case 10:  //埼玉
      wall.add( new PVector( width / 1.67, height / 1.52 ) );
      wall.add( new PVector( width / 1.68, height / 1.54 ) );
      wall.add( new PVector( width / 1.63, height / 1.56 ) );
      wall.add( new PVector( width / 1.61, height / 1.58 ) );
      wall.add( new PVector( width / 1.54, height / 1.56 ) );
      wall.add( new PVector( width / 1.52, height / 1.54 ) );
      wall.add( new PVector( width / 1.52, height / 1.50 ) );
      wall.add( new PVector( width / 1.58, height / 1.50 ) );
      wall.add( new PVector( width / 1.62, height / 1.52 ) );
      wall.add( new PVector( width / 1.68, height / 1.52 ) );
      wall.add( new PVector( width / 1.67, height / 1.52 ) );
      break;
    case 11:  //千葉
      wall.add( new PVector( width / 1.51, height / 1.53 ) );
      wall.add( new PVector( width / 1.47, height / 1.52 ) );
      wall.add( new PVector( width / 1.44, height / 1.52 ) );
      wall.add( new PVector( width / 1.40, height / 1.50 ) );
      wall.add( new PVector( width / 1.44, height / 1.48 ) );
      wall.add( new PVector( width / 1.45, height / 1.46 ) );
      wall.add( new PVector( width / 1.45, height / 1.43 ) );
      wall.add( new PVector( width / 1.49, height / 1.42 ) );
      wall.add( new PVector( width / 1.51, height / 1.40 ) );
      wall.add( new PVector( width / 1.51, height / 1.40 ) );
      wall.add( new PVector( width / 1.52, height / 1.41 ) );
      wall.add( new PVector( width / 1.52, height / 1.45 ) );
      wall.add( new PVector( width / 1.49, height / 1.47 ) );
      wall.add( new PVector( width / 1.50, height / 1.48 ) );
      wall.add( new PVector( width / 1.51, height / 1.48 ) );
      wall.add( new PVector( width / 1.51, height / 1.53 ) );
      break;
    case 12:  //東京
      wall.add( new PVector( width / 1.64, height / 1.52 ) );
      wall.add( new PVector( width / 1.58, height / 1.51 ) );
      wall.add( new PVector( width / 1.515, height / 1.51 ) );
      wall.add( new PVector( width / 1.515, height / 1.49 ) );
      wall.add( new PVector( width / 1.53, height / 1.48 ) );
      wall.add( new PVector( width / 1.56, height / 1.48 ) );
      wall.add( new PVector( width / 1.57, height / 1.47 ) );
      wall.add( new PVector( width / 1.61, height / 1.49 ) );
      wall.add( new PVector( width / 1.64, height / 1.52 ) );
      break;
    case 13:  //神奈川
      wall.add( new PVector( width / 1.61, height / 1.49 ) );
      wall.add( new PVector( width / 1.57, height / 1.47 ) );
      wall.add( new PVector( width / 1.56, height / 1.48 ) );
      wall.add( new PVector( width / 1.53, height / 1.47 ) );
      wall.add( new PVector( width / 1.54, height / 1.45 ) );
      wall.add( new PVector( width / 1.53, height / 1.43 ) );
      wall.add( new PVector( width / 1.55, height / 1.42 ) );
      wall.add( new PVector( width / 1.56, height / 1.43 ) );
      wall.add( new PVector( width / 1.59, height / 1.44 ) );
      wall.add( new PVector( width / 1.61, height / 1.43 ) );
      wall.add( new PVector( width / 1.62, height / 1.42 ) );
      wall.add( new PVector( width / 1.64, height / 1.43 ) );
      wall.add( new PVector( width / 1.63, height / 1.45 ) );
      wall.add( new PVector( width / 1.64, height / 1.45 ) );
      wall.add( new PVector( width / 1.64, height / 1.46 ) );
      wall.add( new PVector( width / 1.61, height / 1.48 ) );
      wall.add( new PVector( width / 1.61, height / 1.49 ) );
      break;
    case 14:  //新潟
      wall.add( new PVector( width / 1.84, height / 1.69 ) );
      wall.add( new PVector( width / 1.71, height / 1.77 ) );
      wall.add( new PVector( width / 1.66, height / 1.86 ) );
      wall.add( new PVector( width / 1.59, height / 1.91 ) );
      wall.add( new PVector( width / 1.56, height / 2.03 ) );
      wall.add( new PVector( width / 1.52, height / 1.98 ) );
      wall.add( new PVector( width / 1.54, height / 1.95 ) );
      wall.add( new PVector( width / 1.55, height / 1.89 ) );
      wall.add( new PVector( width / 1.54, height / 1.86 ) );
      wall.add( new PVector( width / 1.55, height / 1.83 ) );
      wall.add( new PVector( width / 1.55, height / 1.80 ) );
      wall.add( new PVector( width / 1.60, height / 1.78 ) );
      wall.add( new PVector( width / 1.60, height / 1.78 ) );
      wall.add( new PVector( width / 1.60, height / 1.69 ) );
      wall.add( new PVector( width / 1.63, height / 1.70 ) );
      wall.add( new PVector( width / 1.68, height / 1.65 ) );
      wall.add( new PVector( width / 1.71, height / 1.69 ) );
      wall.add( new PVector( width / 1.71, height / 1.69 ) );
      wall.add( new PVector( width / 1.78, height / 1.67 ) );
      wall.add( new PVector( width / 1.80, height / 1.68 ) );
      wall.add( new PVector( width / 1.83, height / 1.66 ) );
      wall.add( new PVector( width / 1.84, height / 1.69 ) );
      break;
    case 15:  //富山
      wall.add( new PVector( width / 2.019, height / 1.591 ) );
      wall.add( new PVector( width / 2.019, height / 1.646 ) );
      wall.add( new PVector( width / 2.002, height / 1.677 ) );
      wall.add( new PVector( width / 1.965, height / 1.695 ) );
      wall.add( new PVector( width / 1.975, height / 1.677 ) );
      wall.add( new PVector( width / 1.937, height / 1.659 ) );
      wall.add( new PVector( width / 1.903, height / 1.669 ) );
      wall.add( new PVector( width / 1.848, height / 1.698 ) );
      wall.add( new PVector( width / 1.836, height / 1.639 ) );
      wall.add( new PVector( width / 1.860, height / 1.595 ) );
      wall.add( new PVector( width / 1.912, height / 1.602 ) );
      wall.add( new PVector( width / 1.945, height / 1.602 ) );
      wall.add( new PVector( width / 1.981, height / 1.579 ) );
      wall.add( new PVector( width / 2.002, height / 1.591 ) );
      wall.add( new PVector( width / 2.019, height / 1.591 ) );
      break;
    case 16:  //石川
      wall.add( new PVector( width / 2.148, height / 1.586 ) );
      wall.add( new PVector( width / 2.078, height / 1.622 ) );
      wall.add( new PVector( width / 2.034, height / 1.690 ) );
      wall.add( new PVector( width / 2.049, height / 1.731 ) );
      wall.add( new PVector( width / 2.034, height / 1.765 ) );
      wall.add( new PVector( width / 1.932, height / 1.803 ) );
      wall.add( new PVector( width / 1.907, height / 1.788 ) );
      wall.add( new PVector( width / 1.932, height / 1.759 ) );
      wall.add( new PVector( width / 1.975, height / 1.736 ) );
      wall.add( new PVector( width / 1.967, height / 1.698 ) );
      wall.add( new PVector( width / 2.013, height / 1.677 ) );
      wall.add( new PVector( width / 2.023, height / 1.639 ) );
      wall.add( new PVector( width / 2.023, height / 1.584 ) );
      wall.add( new PVector( width / 2.038, height / 1.552 ) );
      wall.add( new PVector( width / 2.101, height / 1.563 ) );
      wall.add( new PVector( width / 2.148, height / 1.586 ) );
      break;
    case 17:  //福井
      wall.add( new PVector( width / 2.152, height / 1.579 ) );
      wall.add( new PVector( width / 2.136, height / 1.567 ) );
      wall.add( new PVector( width / 2.045, height / 1.547 ) );
      wall.add( new PVector( width / 2.051, height / 1.536 ) );
      wall.add( new PVector( width / 2.030, height / 1.515 ) );
      wall.add( new PVector( width / 2.067, height / 1.508 ) );
      wall.add( new PVector( width / 2.129, height / 1.504 ) );
      wall.add( new PVector( width / 2.140, height / 1.490 ) );
      wall.add( new PVector( width / 2.177, height / 1.492 ) );
      wall.add( new PVector( width / 2.189, height / 1.475 ) );
      wall.add( new PVector( width / 2.240, height / 1.465 ) );
      wall.add( new PVector( width / 2.288, height / 1.448 ) );
      wall.add( new PVector( width / 2.350, height / 1.448 ) );
      wall.add( new PVector( width / 2.373, height / 1.471 ) );
      wall.add( new PVector( width / 2.302, height / 1.467 ) );
      wall.add( new PVector( width / 2.270, height / 1.481 ) );
      wall.add( new PVector( width / 2.235, height / 1.481 ) );
      wall.add( new PVector( width / 2.197, height / 1.506 ) );
      wall.add( new PVector( width / 2.222, height / 1.534 ) );
      wall.add( new PVector( width / 2.204, height / 1.556 ) );
      wall.add( new PVector( width / 2.152, height / 1.579 ) );
      break;
    case 18:  //山梨
      wall.add( new PVector( width / 1.727, height / 1.528 ) );
      wall.add( new PVector( width / 1.686, height / 1.517 ) );
      wall.add( new PVector( width / 1.641, height / 1.510 ) );
      wall.add( new PVector( width / 1.638, height / 1.513 ) );
      wall.add( new PVector( width / 1.615, height / 1.492 ) );
      wall.add( new PVector( width / 1.615, height / 1.481 ) );
      wall.add( new PVector( width / 1.641, height / 1.452 ) );
      wall.add( new PVector( width / 1.671, height / 1.450 ) );
      wall.add( new PVector( width / 1.701, height / 1.452 ) );
      wall.add( new PVector( width / 1.711, height / 1.427 ) );
      wall.add( new PVector( width / 1.745, height / 1.446 ) );
      wall.add( new PVector( width / 1.755, height / 1.490 ) );
      wall.add( new PVector( width / 1.750, height / 1.517 ) );
      wall.add( new PVector( width / 1.727, height / 1.528 ) );
      break;
    case 19:  //長野
      wall.add( new PVector( width / 1.912, height / 1.517 ) );
      wall.add( new PVector( width / 1.870, height / 1.528 ) );
      wall.add( new PVector( width / 1.870, height / 1.549 ) );
      wall.add( new PVector( width / 1.851, height / 1.574 ) );
      wall.add( new PVector( width / 1.857, height / 1.591 ) );
      wall.add( new PVector( width / 1.830, height / 1.644 ) );
      wall.add( new PVector( width / 1.805, height / 1.677 ) );
      wall.add( new PVector( width / 1.776, height / 1.669 ) );
      wall.add( new PVector( width / 1.711, height / 1.690 ) );
      wall.add( new PVector( width / 1.686, height / 1.651 ) );
      wall.add( new PVector( width / 1.719, height / 1.639 ) );
      wall.add( new PVector( width / 1.731, height / 1.610 ) );
      wall.add( new PVector( width / 1.686, height / 1.591 ) );
      wall.add( new PVector( width / 1.693, height / 1.556 ) );
      wall.add( new PVector( width / 1.678, height / 1.523 ) );
      wall.add( new PVector( width / 1.727, height / 1.528 ) );
      wall.add( new PVector( width / 1.755, height / 1.519 ) );
      wall.add( new PVector( width / 1.758, height / 1.488 ) );
      wall.add( new PVector( width / 1.771, height / 1.452 ) );
      wall.add( new PVector( width / 1.830, height / 1.429 ) );
      wall.add( new PVector( width / 1.870, height / 1.432 ) );

      wall.add( new PVector( width / 1.866, height / 1.467 ) );
      wall.add( new PVector( width / 1.912, height / 1.517 ) );
      break;
    case 20:  //岐阜
      wall.add( new PVector( width / 2.047, height / 1.541 ) );
      wall.add( new PVector( width / 2.021, height / 1.572 ) );
      wall.add( new PVector( width / 2.021, height / 1.584 ) );
      wall.add( new PVector( width / 2.006, height / 1.586 ) );
      wall.add( new PVector( width / 1.986, height / 1.579 ) );
      wall.add( new PVector( width / 1.953, height / 1.595 ) );
      wall.add( new PVector( width / 1.914, height / 1.598 ) );
      wall.add( new PVector( width / 1.864, height / 1.591 ) );
      wall.add( new PVector( width / 1.860, height / 1.579 ) );
      wall.add( new PVector( width / 1.873, height / 1.552 ) );
      wall.add( new PVector( width / 1.873, height / 1.534 ) );
      wall.add( new PVector( width / 1.916, height / 1.517 ) );
      wall.add( new PVector( width / 1.870, height / 1.467 ) );
      wall.add( new PVector( width / 1.877, height / 1.438 ) );
      wall.add( new PVector( width / 1.914, height / 1.432 ) );
      wall.add( new PVector( width / 2.000, height / 1.452 ) );
      wall.add( new PVector( width / 2.043, height / 1.448 ) );
      wall.add( new PVector( width / 2.053, height / 1.429 ) );
      wall.add( new PVector( width / 2.089, height / 1.432 ) );
      wall.add( new PVector( width / 2.119, height / 1.432 ) );
      wall.add( new PVector( width / 2.119, height / 1.457 ) );
      wall.add( new PVector( width / 2.140, height / 1.481 ) );
      wall.add( new PVector( width / 2.124, height / 1.506 ) );
      wall.add( new PVector( width / 2.067, height / 1.502 ) );
      wall.add( new PVector( width / 2.023, height / 1.517 ) );
      wall.add( new PVector( width / 2.047, height / 1.541 ) );
      break;
    case 21:  //静岡
      wall.add( new PVector( width / 1.825, height / 1.429 ) );
      wall.add( new PVector( width / 1.771, height / 1.446 ) );
      wall.add( new PVector( width / 1.760, height / 1.477 ) );
      wall.add( new PVector( width / 1.750, height / 1.448 ) );
      wall.add( new PVector( width / 1.724, height / 1.429 ) );
      wall.add( new PVector( width / 1.705, height / 1.432 ) );
      wall.add( new PVector( width / 1.696, height / 1.448 ) );
      wall.add( new PVector( width / 1.635, height / 1.448 ) );
      wall.add( new PVector( width / 1.640, height / 1.432 ) );
      wall.add( new PVector( width / 1.622, height / 1.417 ) );
      wall.add( new PVector( width / 1.615, height / 1.395 ) );
      wall.add( new PVector( width / 1.657, height / 1.360 ) );
      wall.add( new PVector( width / 1.671, height / 1.369 ) );
      wall.add( new PVector( width / 1.671, height / 1.404 ) );
      wall.add( new PVector( width / 1.649, height / 1.410 ) );
      wall.add( new PVector( width / 1.678, height / 1.423 ) );
      wall.add( new PVector( width / 1.734, height / 1.386 ) );
      wall.add( new PVector( width / 1.760, height / 1.369 ) );
      wall.add( new PVector( width / 1.755, height / 1.355 ) );
      wall.add( new PVector( width / 1.822, height / 1.360 ) );
      wall.add( new PVector( width / 1.884, height / 1.364 ) );
      wall.add( new PVector( width / 1.881, height / 1.383 ) );
      wall.add( new PVector( width / 1.825, height / 1.429 ) );
      break;
    case 22:  //愛知
      wall.add( new PVector( width / 2.053, height / 1.423 ) );
      wall.add( new PVector( width / 2.043, height / 1.446 ) );
      wall.add( new PVector( width / 2.004, height / 1.450 ) );
      wall.add( new PVector( width / 1.907, height / 1.432 ) );
      wall.add( new PVector( width / 1.879, height / 1.436 ) );
      wall.add( new PVector( width / 1.864, height / 1.427 ) );
      wall.add( new PVector( width / 1.835, height / 1.427 ) );
      wall.add( new PVector( width / 1.855, height / 1.399 ) );
      wall.add( new PVector( width / 1.888, height / 1.369 ) );
      wall.add( new PVector( width / 1.975, height / 1.352 ) );
      wall.add( new PVector( width / 1.979, height / 1.358 ) );
      wall.add( new PVector( width / 1.920, height / 1.372 ) );
      wall.add( new PVector( width / 1.930, height / 1.378 ) );
      wall.add( new PVector( width / 1.965, height / 1.378 ) );
      wall.add( new PVector( width / 1.990, height / 1.385 ) );
      wall.add( new PVector( width / 1.990, height / 1.369 ) );
      wall.add( new PVector( width / 2.010, height / 1.376 ) );
      wall.add( new PVector( width / 2.010, height / 1.408 ) );
      wall.add( new PVector( width / 2.032, height / 1.408 ) );
      wall.add( new PVector( width / 2.053, height / 1.423 ) );
      break;
    case 23:  //三重
      wall.add( new PVector( width / 2.199, height / 1.378 ) );
      wall.add( new PVector( width / 2.169, height / 1.390 ) );
      wall.add( new PVector( width / 2.122, height / 1.394 ) );
      wall.add( new PVector( width / 2.110, height / 1.399 ) );
      wall.add( new PVector( width / 2.115, height / 1.427 ) );
      wall.add( new PVector( width / 2.069, height / 1.427 ) );
      wall.add( new PVector( width / 2.043, height / 1.408 ) );
      wall.add( new PVector( width / 2.087, height / 1.372 ) );
      wall.add( new PVector( width / 2.000, height / 1.342 ) );
      wall.add( new PVector( width / 2.010, height / 1.322 ) );
      wall.add( new PVector( width / 2.080, height / 1.322 ) );
      wall.add( new PVector( width / 2.145, height / 1.311 ) );
      wall.add( new PVector( width / 2.145, height / 1.295 ) );
      wall.add( new PVector( width / 2.207, height / 1.275 ) );
      wall.add( new PVector( width / 2.215, height / 1.260 ) );
      wall.add( new PVector( width / 2.259, height / 1.278 ) );
      wall.add( new PVector( width / 2.199, height / 1.301 ) );
      wall.add( new PVector( width / 2.194, height / 1.330 ) );
      wall.add( new PVector( width / 2.157, height / 1.350 ) );
      wall.add( new PVector( width / 2.199, height / 1.350 ) );
      wall.add( new PVector( width / 2.199, height / 1.350 ) );
      wall.add( new PVector( width / 2.202, height / 1.376 ) );
      wall.add( new PVector( width / 2.199, height / 1.378 ) );
      break;
    case 24:  //滋賀
      wall.add( new PVector( width / 2.278, height / 1.446 ) );
      wall.add( new PVector( width / 2.246, height / 1.459 ) );
      wall.add( new PVector( width / 2.187, height / 1.469 ) );
      wall.add( new PVector( width / 2.174, height / 1.486 ) );
      wall.add( new PVector( width / 2.145, height / 1.486 ) );
      wall.add( new PVector( width / 2.145, height / 1.475 ) );
      wall.add( new PVector( width / 2.129, height / 1.456 ) );
      wall.add( new PVector( width / 2.129, height / 1.427 ) );
      wall.add( new PVector( width / 2.117, height / 1.399 ) );
      wall.add( new PVector( width / 2.177, height / 1.390 ) );
      wall.add( new PVector( width / 2.207, height / 1.381 ) );
      wall.add( new PVector( width / 2.264, height / 1.403 ) );
      wall.add( new PVector( width / 2.278, height / 1.446 ) );
      break;
    case 25:  //京都
      wall.add( new PVector( width / 2.563, height / 1.486 ) );
      wall.add( new PVector( width / 2.520, height / 1.490 ) );
      wall.add( new PVector( width / 2.449, height / 1.502 ) );
      wall.add( new PVector( width / 2.427, height / 1.502 ) );
      wall.add( new PVector( width / 2.427, height / 1.481 ) );
      wall.add( new PVector( width / 2.440, height / 1.475 ) );
      wall.add( new PVector( width / 2.418, height / 1.471 ) );
      wall.add( new PVector( width / 2.382, height / 1.471 ) );
      wall.add( new PVector( width / 2.359, height / 1.446 ) );
      wall.add( new PVector( width / 2.283, height / 1.442 ) );
      wall.add( new PVector( width / 2.275, height / 1.408 ) );
      wall.add( new PVector( width / 2.215, height / 1.378 ) );
      wall.add( new PVector( width / 2.288, height / 1.376 ) );
      wall.add( new PVector( width / 2.316, height / 1.399 ) );
      wall.add( new PVector( width / 2.344, height / 1.395 ) );
      wall.add( new PVector( width / 2.388, height / 1.408 ) );
      wall.add( new PVector( width / 2.403, height / 1.421 ) );
      wall.add( new PVector( width / 2.546, height / 1.450 ) );
      wall.add( new PVector( width / 2.507, height / 1.465 ) );
      wall.add( new PVector( width / 2.563, height / 1.475 ) );
      wall.add( new PVector( width / 2.563, height / 1.486 ) );
      break;
    case 26:  //大阪
      wall.add( new PVector( width / 2.388, height / 1.404 ) );
      wall.add( new PVector( width / 2.310, height / 1.392 ) );
      wall.add( new PVector( width / 2.302, height / 1.378 ) );
      wall.add( new PVector( width / 2.308, height / 1.352 ) );
      wall.add( new PVector( width / 2.316, height / 1.335 ) );
      wall.add( new PVector( width / 2.481, height / 1.327 ) );
      wall.add( new PVector( width / 2.412, height / 1.343 ) );
      wall.add( new PVector( width / 2.379, height / 1.378 ) );
      wall.add( new PVector( width / 2.388, height / 1.404 ) );
      break;
    case 27:  //兵庫
      wall.add( new PVector( width / 2.743, height / 1.479 ) );
      wall.add( new PVector( width / 2.697, height / 1.486 ) );
      wall.add( new PVector( width / 2.570, height / 1.486 ) );
      wall.add( new PVector( width / 2.577, height / 1.475 ) );
      wall.add( new PVector( width / 2.513, height / 1.465 ) );
      wall.add( new PVector( width / 2.560, height / 1.450 ) );
      wall.add( new PVector( width / 2.409, height / 1.417 ) );
      wall.add( new PVector( width / 2.409, height / 1.395 ) );
      wall.add( new PVector( width / 2.388, height / 1.386 ) );
      wall.add( new PVector( width / 2.394, height / 1.369 ) );
      wall.add( new PVector( width / 2.440, height / 1.369 ) );
      wall.add( new PVector( width / 2.494, height / 1.360 ) );
      wall.add( new PVector( width / 2.595, height / 1.376 ) );
      wall.add( new PVector( width / 2.697, height / 1.376 ) );
      wall.add( new PVector( width / 2.775, height / 1.372 ) );
      wall.add( new PVector( width / 2.803, height / 1.390 ) );
      wall.add( new PVector( width / 2.755, height / 1.430 ) );
      wall.add( new PVector( width / 2.697, height / 1.442 ) );
      wall.add( new PVector( width / 2.743, height / 1.479 ) );
      break;
    case 28:  //奈良
      wall.add( new PVector( width / 2.294, height / 1.372 ) );
      wall.add( new PVector( width / 2.207, height / 1.374 ) );
      wall.add( new PVector( width / 2.207, height / 1.352 ) );
      wall.add( new PVector( width / 2.169, height / 1.343 ) );
      wall.add( new PVector( width / 2.202, height / 1.332 ) );
      wall.add( new PVector( width / 2.202, height / 1.303 ) );
      wall.add( new PVector( width / 2.264, height / 1.283 ) );
      wall.add( new PVector( width / 2.319, height / 1.283 ) );
      wall.add( new PVector( width / 2.341, height / 1.303 ) );
      wall.add( new PVector( width / 2.299, height / 1.319 ) );
      wall.add( new PVector( width / 2.313, height / 1.335 ) );
      wall.add( new PVector( width / 2.294, height / 1.372 ) );
      break;
    case 29:  //和歌山
      wall.add( new PVector( width / 2.494, height / 1.322 ) );
      wall.add( new PVector( width / 2.316, height / 1.330 ) );
      wall.add( new PVector( width / 2.308, height / 1.319 ) );
      wall.add( new PVector( width / 2.344, height / 1.303 ) );
      wall.add( new PVector( width / 2.327, height / 1.280 ) );
      wall.add( new PVector( width / 2.267, height / 1.280 ) );
      wall.add( new PVector( width / 2.222, height / 1.257 ) );
      wall.add( new PVector( width / 2.280, height / 1.239 ) );
      wall.add( new PVector( width / 2.400, height / 1.253 ) );
      wall.add( new PVector( width / 2.400, height / 1.268 ) );
      wall.add( new PVector( width / 2.497, height / 1.286 ) );
      wall.add( new PVector( width / 2.471, height / 1.293 ) );
      wall.add( new PVector( width / 2.494, height / 1.303 ) );
      wall.add( new PVector( width / 2.455, height / 1.311 ) );
      wall.add( new PVector( width / 2.494, height / 1.322 ) );
      break;
    case 30:  //鳥取
      wall.add( new PVector( width / 3.243, height / 1.461 ) );
      wall.add( new PVector( width / 3.189, height / 1.461 ) );
      wall.add( new PVector( width / 3.122, height / 1.471 ) );
      wall.add( new PVector( width / 2.896, height / 1.467 ) );
      wall.add( new PVector( width / 2.755, height / 1.477 ) );
      wall.add( new PVector( width / 2.704, height / 1.442 ) );
      wall.add( new PVector( width / 2.763, height / 1.432 ) );
      wall.add( new PVector( width / 2.866, height / 1.432 ) );
      wall.add( new PVector( width / 2.931, height / 1.442 ) );
      wall.add( new PVector( width / 3.000, height / 1.438 ) );
      wall.add( new PVector( width / 3.097, height / 1.448 ) );
      wall.add( new PVector( width / 3.293, height / 1.414 ) );
      wall.add( new PVector( width / 3.339, height / 1.414 ) );
      wall.add( new PVector( width / 3.351, height / 1.427 ) );
      wall.add( new PVector( width / 3.265, height / 1.442 ) );
      wall.add( new PVector( width / 3.243, height / 1.461 ) );
      break;
    case 31:  //島根
      wall.add( new PVector( width / 4.571, height / 1.362 ) );
      wall.add( new PVector( width / 3.699, height / 1.440 ) );
      wall.add( new PVector( width / 3.714, height / 1.454 ) );
      wall.add( new PVector( width / 3.404, height / 1.475 ) );
      wall.add( new PVector( width / 3.328, height / 1.479 ) );
      wall.add( new PVector( width / 3.260, height / 1.465 ) );
      wall.add( new PVector( width / 3.282, height / 1.446 ) );
      wall.add( new PVector( width / 3.368, height / 1.430 ) );
      wall.add( new PVector( width / 3.368, height / 1.417 ) );
      wall.add( new PVector( width / 3.556, height / 1.415 ) );
      wall.add( new PVector( width / 3.678, height / 1.394 ) );
      wall.add( new PVector( width / 4.111, height / 1.372 ) );
      wall.add( new PVector( width / 4.220, height / 1.345 ) );
      wall.add( new PVector( width / 4.374, height / 1.330 ) );
      wall.add( new PVector( width / 4.571, height / 1.342 ) );
      wall.add( new PVector( width / 4.571, height / 1.362 ) );
      break;
    case 32:  //岡山
      wall.add( new PVector( width / 3.282, height / 1.408 ) );
      wall.add( new PVector( width / 3.087, height / 1.446 ) );
      wall.add( new PVector( width / 3.000, height / 1.436 ) );
      wall.add( new PVector( width / 2.931, height / 1.440 ) );
      wall.add( new PVector( width / 2.866, height / 1.430 ) );
      wall.add( new PVector( width / 2.763, height / 1.430 ) );
      wall.add( new PVector( width / 2.803, height / 1.395 ) );
      wall.add( new PVector( width / 2.791, height / 1.372 ) );
      wall.add( new PVector( width / 2.844, height / 1.369 ) );
      wall.add( new PVector( width / 2.857, height / 1.355 ) );
      wall.add( new PVector( width / 2.954, height / 1.343 ) );
      wall.add( new PVector( width / 3.048, height / 1.343 ) );
      wall.add( new PVector( width / 3.097, height / 1.347 ) );
      wall.add( new PVector( width / 3.174, height / 1.342 ) );
      wall.add( new PVector( width / 3.254, height / 1.372 ) );
      wall.add( new PVector( width / 3.282, height / 1.390 ) );
      wall.add( new PVector( width / 3.282, height / 1.408 ) );
      break;
    case 33:  //広島
      wall.add( new PVector( width / 4.201, height / 1.342 ) );
      wall.add( new PVector( width / 4.085, height / 1.372 ) );
      wall.add( new PVector( width / 3.692, height / 1.388 ) );
      wall.add( new PVector( width / 3.542, height / 1.412 ) );
      wall.add( new PVector( width / 3.310, height / 1.412 ) );
      wall.add( new PVector( width / 3.282, height / 1.385 ) );
      wall.add( new PVector( width / 3.189, height / 1.345 ) );
      wall.add( new PVector( width / 3.254, height / 1.333 ) );
      wall.add( new PVector( width / 3.374, height / 1.337 ) );
      wall.add( new PVector( width / 3.435, height / 1.328 ) );
      wall.add( new PVector( width / 3.556, height / 1.328 ) );
      wall.add( new PVector( width / 3.664, height / 1.317 ) );
      wall.add( new PVector( width / 3.787, height / 1.317 ) );
      wall.add( new PVector( width / 3.787, height / 1.328 ) );
      wall.add( new PVector( width / 3.840, height / 1.333 ) );
      wall.add( new PVector( width / 3.943, height / 1.330 ) );
      wall.add( new PVector( width / 4.008, height / 1.317 ) );
      wall.add( new PVector( width / 4.111, height / 1.328 ) );
      wall.add( new PVector( width / 4.201, height / 1.342 ) );
      break;
    case 34:  //山口
      wall.add( new PVector( width / 5.731, height / 1.325 ) );
      wall.add( new PVector( width / 5.424, height / 1.337 ) );
      wall.add( new PVector( width / 5.161, height / 1.333 ) );
      wall.add( new PVector( width / 4.923, height / 1.342 ) );
      wall.add( new PVector( width / 4.694, height / 1.362 ) );
      wall.add( new PVector( width / 4.604, height / 1.362 ) );
      wall.add( new PVector( width / 4.604, height / 1.342 ) );
      wall.add( new PVector( width / 4.394, height / 1.325 ) );
      wall.add( new PVector( width / 4.201, height / 1.333 ) );
      wall.add( new PVector( width / 4.138, height / 1.325 ) );
      wall.add( new PVector( width / 4.025, height / 1.314 ) );
      wall.add( new PVector( width / 4.051, height / 1.297 ) );
      wall.add( new PVector( width / 4.129, height / 1.278 ) );
      wall.add( new PVector( width / 4.344, height / 1.289 ) );
      wall.add( new PVector( width / 4.550, height / 1.301 ) );
      wall.add( new PVector( width / 4.741, height / 1.293 ) );
      wall.add( new PVector( width / 4.923, height / 1.293 ) );
      wall.add( new PVector( width / 5.120, height / 1.286 ) );
      wall.add( new PVector( width / 5.455, height / 1.297 ) );
      wall.add( new PVector( width / 5.614, height / 1.290 ) );
      wall.add( new PVector( width / 5.749, height / 1.301 ) );
      wall.add( new PVector( width / 5.614, height / 1.317 ) );
      wall.add( new PVector( width / 5.731, height / 1.325 ) );
      break;
    case 35:  //徳島
      wall.add( new PVector( width / 3.062, height / 1.283 ) );
      wall.add( new PVector( width / 3.024, height / 1.301 ) );
      wall.add( new PVector( width / 2.844, height / 1.309 ) );
      wall.add( new PVector( width / 2.743, height / 1.309 ) );
      wall.add( new PVector( width / 2.743, height / 1.309 ) );
      wall.add( new PVector( width / 2.716, height / 1.314 ) );
      wall.add( new PVector( width / 2.659, height / 1.314 ) );
      wall.add( new PVector( width / 2.667, height / 1.295 ) );
      wall.add( new PVector( width / 2.634, height / 1.275 ) );
      wall.add( new PVector( width / 2.803, height / 1.246 ) );
      wall.add( new PVector( width / 2.900, height / 1.271 ) );
      wall.add( new PVector( width / 2.981, height / 1.272 ) );
      wall.add( new PVector( width / 3.062, height / 1.283 ) );
      break;
    case 36:  //香川
      wall.add( new PVector( width / 3.112, height / 1.317 ) );
      wall.add( new PVector( width / 3.038, height / 1.317 ) );
      wall.add( new PVector( width / 2.968, height / 1.330 ) );
      wall.add( new PVector( width / 2.914, height / 1.330 ) );
      wall.add( new PVector( width / 2.857, height / 1.335 ) );
      wall.add( new PVector( width / 2.815, height / 1.322 ) );
      wall.add( new PVector( width / 2.735, height / 1.314 ) );
      wall.add( new PVector( width / 2.767, height / 1.311 ) );
      wall.add( new PVector( width / 2.836, height / 1.311 ) );
      wall.add( new PVector( width / 2.900, height / 1.306 ) );
      wall.add( new PVector( width / 3.028, height / 1.303 ) );
      wall.add( new PVector( width / 3.062, height / 1.290 ) );
      wall.add( new PVector( width / 3.102, height / 1.295 ) );
      wall.add( new PVector( width / 3.087, height / 1.306 ) );
      wall.add( new PVector( width / 3.112, height / 1.317 ) );
      break;
    case 37:  //愛媛
      wall.add( new PVector( width / 4.229, height / 1.233 ) );
      wall.add( new PVector( width / 3.678, height / 1.262 ) );
      wall.add( new PVector( width / 3.664, height / 1.283 ) );
      wall.add( new PVector( width / 3.485, height / 1.306 ) );
      wall.add( new PVector( width / 3.392, height / 1.287 ) );
      wall.add( new PVector( width / 3.127, height / 1.295 ) );
      wall.add( new PVector( width / 3.067, height / 1.290 ) );
      wall.add( new PVector( width / 3.087, height / 1.284 ) );
      wall.add( new PVector( width / 3.333, height / 1.272 ) );
      wall.add( new PVector( width / 3.416, height / 1.260 ) );
      wall.add( new PVector( width / 3.435, height / 1.246 ) );
      wall.add( new PVector( width / 3.575, height / 1.243 ) );
      wall.add( new PVector( width / 3.549, height / 1.233 ) );
      wall.add( new PVector( width / 3.664, height / 1.219 ) );
      wall.add( new PVector( width / 3.714, height / 1.192 ) );
      wall.add( new PVector( width / 3.848, height / 1.195 ) );
      wall.add( new PVector( width / 3.832, height / 1.222 ) );
      wall.add( new PVector( width / 3.902, height / 1.229 ) );
      wall.add( new PVector( width / 3.926, height / 1.239 ) );
      wall.add( new PVector( width / 4.229, height / 1.233 ) );
      break;
    case 38:  //高知
      wall.add( new PVector( width / 3.699, height / 1.193 ) );
      wall.add( new PVector( width / 3.643, height / 1.215 ) );
      wall.add( new PVector( width / 3.529, height / 1.231 ) );
      wall.add( new PVector( width / 3.542, height / 1.239 ) );
      wall.add( new PVector( width / 3.429, height / 1.243 ) );
      wall.add( new PVector( width / 3.386, height / 1.260 ) );
      wall.add( new PVector( width / 3.316, height / 1.271 ) );
      wall.add( new PVector( width / 3.077, height / 1.280 ) );
      wall.add( new PVector( width / 3.005, height / 1.272 ) );
      wall.add( new PVector( width / 2.900, height / 1.271 ) );
      wall.add( new PVector( width / 2.815, height / 1.246 ) );
      wall.add( new PVector( width / 2.836, height / 1.224 ) );
      wall.add( new PVector( width / 2.857, height / 1.224 ) );
      wall.add( new PVector( width / 2.977, height / 1.246 ) );
      wall.add( new PVector( width / 3.072, height / 1.249 ) );
      wall.add( new PVector( width / 3.189, height / 1.239 ) );
      wall.add( new PVector( width / 3.316, height / 1.229 ) );
      wall.add( new PVector( width / 3.316, height / 1.218 ) );
      wall.add( new PVector( width / 3.459, height / 1.200 ) );
      wall.add( new PVector( width / 3.459, height / 1.178 ) );
      wall.add( new PVector( width / 3.529, height / 1.182 ) );
      wall.add( new PVector( width / 3.596, height / 1.178 ) );
      wall.add( new PVector( width / 3.714, height / 1.180 ) );
      wall.add( new PVector( width / 3.699, height / 1.193 ) );
      break;
    case 39:  //福岡
      wall.add( new PVector( width / 7.559, height / 1.246 ) );
      wall.add( new PVector( width / 7.191, height / 1.256 ) );
      wall.add( new PVector( width / 6.931, height / 1.251 ) );
      wall.add( new PVector( width / 6.531, height / 1.271 ) );
      wall.add( new PVector( width / 6.421, height / 1.278 ) );
      wall.add( new PVector( width / 6.057, height / 1.286 ) );
      wall.add( new PVector( width / 5.697, height / 1.283 ) );
      wall.add( new PVector( width / 5.614, height / 1.286 ) );
      wall.add( new PVector( width / 5.455, height / 1.281 ) );
      wall.add( new PVector( width / 5.614, height / 1.275 ) );
      wall.add( new PVector( width / 5.304, height / 1.253 ) );
      wall.add( new PVector( width / 5.304, height / 1.246 ) );
      wall.add( new PVector( width / 5.647, height / 1.241 ) );
      wall.add( new PVector( width / 5.818, height / 1.234 ) );
      wall.add( new PVector( width / 5.783, height / 1.213 ) );
      wall.add( new PVector( width / 5.783, height / 1.207 ) );
      wall.add( new PVector( width / 6.057, height / 1.213 ) );
      wall.add( new PVector( width / 6.358, height / 1.207 ) );
      wall.add( new PVector( width / 6.508, height / 1.197 ) );
      wall.add( new PVector( width / 6.690, height / 1.197 ) );
      wall.add( new PVector( width / 6.690, height / 1.207 ) );
      wall.add( new PVector( width / 6.857, height / 1.213 ) );
      wall.add( new PVector( width / 6.358, height / 1.230 ) );
      wall.add( new PVector( width / 6.358, height / 1.237 ) );
      wall.add( new PVector( width / 6.690, height / 1.234 ) );
      wall.add( new PVector( width / 7.471, height / 1.241 ) );
      wall.add( new PVector( width / 7.559, height / 1.246 ) );
      break;
    case 40:  //佐賀
      wall.add( new PVector( width / 8.571, height / 1.227 ) );
      wall.add( new PVector( width / 8.205, height / 1.227 ) );
      wall.add( new PVector( width / 8.533, height / 1.239 ) );
      wall.add( new PVector( width / 8.348, height / 1.249 ) );
      wall.add( new PVector( width / 8.033, height / 1.249 ) );
      wall.add( new PVector( width / 8.033, height / 1.241 ) );
      wall.add( new PVector( width / 7.619, height / 1.241 ) );
      wall.add( new PVector( width / 6.737, height / 1.234 ) );
      wall.add( new PVector( width / 6.421, height / 1.237 ) );
      wall.add( new PVector( width / 6.421, height / 1.231 ) );
      wall.add( new PVector( width / 6.931, height / 1.213 ) );
      wall.add( new PVector( width / 7.191, height / 1.218 ) );
      wall.add( new PVector( width / 7.471, height / 1.211 ) );
      wall.add( new PVector( width / 7.191, height / 1.197 ) );
      wall.add( new PVector( width / 7.529, height / 1.197 ) );
      wall.add( new PVector( width / 8.276, height / 1.216 ) );
      wall.add( new PVector( width / 8.571, height / 1.227 ) );
      break;
    case 41:  //長崎
      wall.add( new PVector( width / 9.505, height / 1.231 ) );
      wall.add( new PVector( width / 8.767, height / 1.231 ) );
      wall.add( new PVector( width / 8.727, height / 1.222 ) );
      wall.add( new PVector( width / 7.680, height / 1.197 ) );
      wall.add( new PVector( width / 7.245, height / 1.197 ) );
      wall.add( new PVector( width / 7.471, height / 1.188 ) );
      wall.add( new PVector( width / 7.059, height / 1.186 ) );
      wall.add( new PVector( width / 6.931, height / 1.188 ) );
      wall.add( new PVector( width / 6.809, height / 1.179 ) );
      wall.add( new PVector( width / 7.059, height / 1.169 ) );
      wall.add( new PVector( width / 7.471, height / 1.171 ) );
      wall.add( new PVector( width / 7.191, height / 1.178 ) );
      wall.add( new PVector( width / 7.619, height / 1.182 ) );
      wall.add( new PVector( width / 8.101, height / 1.175 ) );
      wall.add( new PVector( width / 8.348, height / 1.169 ) );
      wall.add( new PVector( width / 8.649, height / 1.165 ) );
      wall.add( new PVector( width / 8.384, height / 1.178 ) );
      wall.add( new PVector( width / 8.930, height / 1.184 ) );
      wall.add( new PVector( width / 9.275, height / 1.188 ) );
      wall.add( new PVector( width / 9.275, height / 1.204 ) );
      wall.add( new PVector( width / 8.848, height / 1.201 ) );
      wall.add( new PVector( width / 8.458, height / 1.188 ) );
      wall.add( new PVector( width / 7.934, height / 1.186 ) );
      wall.add( new PVector( width / 8.101, height / 1.201 ) );
      wall.add( new PVector( width / 8.848, height / 1.204 ) );
      wall.add( new PVector( width / 9.600, height / 1.219 ) );
      wall.add( new PVector( width / 9.505, height / 1.231 ) );
      break;
    case 42:  //熊本
      wall.add( new PVector( width / 6.621, height / 1.195 ) );
      wall.add( new PVector( width / 6.421, height / 1.197 ) );
      wall.add( new PVector( width / 6.358, height / 1.204 ) );
      wall.add( new PVector( width / 6.057, height / 1.211 ) );
      wall.add( new PVector( width / 5.614, height / 1.201 ) );
      wall.add( new PVector( width / 5.424, height / 1.201 ) );
      wall.add( new PVector( width / 5.501, height / 1.211 ) );
      wall.add( new PVector( width / 5.275, height / 1.208 ) );
      wall.add( new PVector( width / 5.093, height / 1.186 ) );
      wall.add( new PVector( width / 5.378, height / 1.169 ) );
      wall.add( new PVector( width / 5.565, height / 1.160 ) );
      wall.add( new PVector( width / 5.408, height / 1.146 ) );
      wall.add( new PVector( width / 5.455, height / 1.132 ) );
      wall.add( new PVector( width / 6.057, height / 1.126 ) );
      wall.add( new PVector( width / 6.254, height / 1.132 ) );
      wall.add( new PVector( width / 6.809, height / 1.132 ) );
      wall.add( new PVector( width / 6.154, height / 1.171 ) );
      wall.add( new PVector( width / 6.621, height / 1.165 ) );
      wall.add( new PVector( width / 6.621, height / 1.171 ) );
      wall.add( new PVector( width / 6.214, height / 1.182 ) );
      wall.add( new PVector( width / 6.465, height / 1.188 ) );
      wall.add( new PVector( width / 6.621, height / 1.195 ) );
      break;
    case 43:  //大分
      wall.add( new PVector( width / 5.731, height / 1.208 ) );
      wall.add( new PVector( width / 5.783, height / 1.231 ) );
      wall.add( new PVector( width / 5.614, height / 1.240 ) );
      wall.add( new PVector( width / 5.275, height / 1.243 ) );
      wall.add( new PVector( width / 5.275, height / 1.253 ) );
      wall.add( new PVector( width / 4.936, height / 1.250 ) );
      wall.add( new PVector( width / 4.741, height / 1.260 ) );
      wall.add( new PVector( width / 4.582, height / 1.257 ) );
      wall.add( new PVector( width / 4.518, height / 1.246 ) );
      wall.add( new PVector( width / 4.627, height / 1.233 ) );
      wall.add( new PVector( width / 4.776, height / 1.231 ) );
      wall.add( new PVector( width / 4.800, height / 1.222 ) );
      wall.add( new PVector( width / 4.344, height / 1.222 ) );
      wall.add( new PVector( width / 4.444, height / 1.208 ) );
      wall.add( new PVector( width / 4.267, height / 1.205 ) );
      wall.add( new PVector( width / 4.364, height / 1.199 ) );
      wall.add( new PVector( width / 4.248, height / 1.195 ) );
      wall.add( new PVector( width / 4.295, height / 1.184 ) );
      wall.add( new PVector( width / 4.394, height / 1.179 ) );
      wall.add( new PVector( width / 4.518, height / 1.186 ) );
      wall.add( new PVector( width / 4.660, height / 1.182 ) );
      wall.add( new PVector( width / 4.717, height / 1.178 ) );
      wall.add( new PVector( width / 5.066, height / 1.188 ) );
      wall.add( new PVector( width / 5.161, height / 1.205 ) );
      wall.add( new PVector( width / 5.333, height / 1.211 ) );
      wall.add( new PVector( width / 5.565, height / 1.212 ) );
      wall.add( new PVector( width / 5.486, height / 1.205 ) );
      wall.add( new PVector( width / 5.731, height / 1.208 ) );
      break;
    case 44:  //宮崎
      wall.add( new PVector( width / 6.019, height / 1.124 ) );
      wall.add( new PVector( width / 5.378, height / 1.134 ) );
      wall.add( new PVector( width / 5.378, height / 1.144 ) );
      wall.add( new PVector( width / 5.533, height / 1.159 ) );
      wall.add( new PVector( width / 5.093, height / 1.184 ) );
      wall.add( new PVector( width / 4.717, height / 1.178 ) );
      wall.add( new PVector( width / 4.528, height / 1.186 ) );
      wall.add( new PVector( width / 4.424, height / 1.175 ) );
      wall.add( new PVector( width / 4.638, height / 1.150 ) );
      wall.add( new PVector( width / 4.861, height / 1.118 ) );
      wall.add( new PVector( width / 4.873, height / 1.095 ) );
      wall.add( new PVector( width / 5.000, height / 1.087 ) );
      wall.add( new PVector( width / 5.000, height / 1.076 ) );
      wall.add( new PVector( width / 5.093, height / 1.076 ) );
      wall.add( new PVector( width / 5.304, height / 1.081 ) );
      wall.add( new PVector( width / 5.203, height / 1.092 ) );
      wall.add( new PVector( width / 5.455, height / 1.095 ) );
      wall.add( new PVector( width / 5.749, height / 1.103 ) );
      wall.add( new PVector( width / 5.731, height / 1.112 ) );
      wall.add( new PVector( width / 6.019, height / 1.124 ) );
      break;
    case 45:  //鹿児島
      wall.add( new PVector( width / 7.328, height / 1.138 ) );
      wall.add( new PVector( width / 7.328, height / 1.130 ) );
      wall.add( new PVector( width / 7.138, height / 1.126 ) );
      wall.add( new PVector( width / 6.809, height / 1.130 ) );
      wall.add( new PVector( width / 6.295, height / 1.130 ) );
      wall.add( new PVector( width / 6.115, height / 1.124 ) );
      wall.add( new PVector( width / 5.783, height / 1.112 ) );
      wall.add( new PVector( width / 5.783, height / 1.103 ) );
      wall.add( new PVector( width / 5.455, height / 1.092 ) );
      wall.add( new PVector( width / 5.275, height / 1.090 ) );
      wall.add( new PVector( width / 5.304, height / 1.081 ) );
      wall.add( new PVector( width / 5.533, height / 1.081 ) );
      wall.add( new PVector( width / 5.533, height / 1.076 ) );
      wall.add( new PVector( width / 5.378, height / 1.070 ) );
      wall.add( new PVector( width / 5.647, height / 1.060 ) );
      wall.add( new PVector( width / 6.154, height / 1.053 ) );
      wall.add( new PVector( width / 6.154, height / 1.057 ) );
      wall.add( new PVector( width / 5.926, height / 1.062 ) );
      wall.add( new PVector( width / 5.926, height / 1.076 ) );
      wall.add( new PVector( width / 6.095, height / 1.083 ) );
      wall.add( new PVector( width / 6.254, height / 1.087 ) );
      wall.add( new PVector( width / 6.194, height / 1.092 ) );
      wall.add( new PVector( width / 6.019, height / 1.090 ) );
      wall.add( new PVector( width / 5.872, height / 1.094 ) );
      wall.add( new PVector( width / 6.194, height / 1.100 ) );
      wall.add( new PVector( width / 6.508, height / 1.087 ) );
      wall.add( new PVector( width / 6.508, height / 1.074 ) );
      wall.add( new PVector( width / 6.194, height / 1.065 ) );
      wall.add( new PVector( width / 6.295, height / 1.060 ) );
      wall.add( new PVector( width / 6.575, height / 1.062 ) );
      wall.add( new PVector( width / 6.621, height / 1.067 ) );
      wall.add( new PVector( width / 7.191, height / 1.068 ) );
      wall.add( new PVector( width / 7.529, height / 1.076 ) );
      wall.add( new PVector( width / 7.191, height / 1.076 ) );
      wall.add( new PVector( width / 6.882, height / 1.087 ) );
      wall.add( new PVector( width / 7.413, height / 1.098 ) );
      wall.add( new PVector( width / 7.471, height / 1.109 ) );
      wall.add( new PVector( width / 7.245, height / 1.112 ) );
      wall.add( new PVector( width / 7.328, height / 1.126 ) );
      wall.add( new PVector( width / 7.619, height / 1.129 ) );
      wall.add( new PVector( width / 7.559, height / 1.138 ) );
      wall.add( new PVector( width / 7.328, height / 1.138 ) );
      break;
    case 46:  //沖縄
      wall.add( new PVector( width / 27.429, height / 1.050 ) );
      wall.add( new PVector( width / 23.415, height / 1.050 ) );
      wall.add( new PVector( width / 21.333, height / 1.044 ) );
      wall.add( new PVector( width / 16.696, height / 1.058 ) );
      wall.add( new PVector( width / 15.360, height / 1.050 ) );
      wall.add( new PVector( width / 16.696, height / 1.044 ) );
      wall.add( new PVector( width / 28.235, height / 1.032 ) );
      wall.add( new PVector( width / 28.235, height / 1.025 ) );
      wall.add( new PVector( width / 33.103, height / 1.022 ) );
      wall.add( new PVector( width / 32.000, height / 1.015 ) );
      wall.add( new PVector( width / 40.000, height / 1.010 ) );
      wall.add( new PVector( width / 50.526, height / 1.018 ) );
      wall.add( new PVector( width / 38.400, height / 1.025 ) );
      wall.add( new PVector( width / 38.400, height / 1.032 ) );
      wall.add( new PVector( width / 24.000, height / 1.039 ) );
      wall.add( new PVector( width / 27.429, height / 1.050 ) );
      break;
    default:
  }
  return wall;
}