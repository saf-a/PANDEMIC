class Phases{
  private int shareKnowledgeSrc;
  
  Phases(){}
  
  void gameMainPhase(){
    ImageSet images;
    player p = players.get( draws.targetPlayerNo );
    int i, prePosition = p.position;
    prefecture t;
    IntList lst = todoufuken.get( p.position ).adjacent;  //自動車または船による移動
    
    ////自動車または船による移動
    for( i = 0; i < lst.size(); i++ ){
      t = todoufuken.get( lst.get( i ) );
      if( t.isHit() && p.position != t.position ){
        p.move( t.position );
        ACTION_COUNT--;
      }
    }
    
    ////シャトル便による移動
    for( i = 0; i < gameStatus.researchStationList.size(); i++ ){
      t = todoufuken.get( gameStatus.researchStationList.get( i ) );
      if( t.isHit() && p.position != gameStatus.researchStationList.get( i ) ){
        p.move( t.position );
        ACTION_COUNT--;
      }
    }
    
    ////直行便による移動
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
    
    ////チャーター便による移動&作戦エキスパートの特殊技能、イベントカード"空輸"、"政府の補助"
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
        
    ////調査基地の設置
    if( playerActionImgSet.get(1).isHit() && todoufuken.get( p.position ).researchStation != true ){
      if( p.role == ROLES.OPERATIONS_EXPERT ){
         phase = PHASE.SPECIAL_SKILL;
      }else if( searchList( p.cards, p.position ) ){
        todoufuken.get( p.position ).setResearchStation();
        p.removeCard( p.position );
        ACTION_COUNT--;
      }
    }
    
    ////感染者の治療
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
      //治療薬が作成された状態で職業が衛生兵の時、アクションを消費せずに治療する
      if( p.role == ROLES.MEDIC && gameStatus.cureMarkersFlag[i] >= 1 && p.position != prePosition && todoufuken.get( p.position ).pathogenCnt[i] >= 1 ){
        addPathogenProcess( p.position, i, CURE );
      }
      //病原体の数をカウントする
      for( int j = 0; j < todoufuken.size() - eventCardNum; j++ ){
        t = todoufuken.get( j );
        if( t.col == i ){
          cnt[i] += t.pathogenCnt[t.col];
        }
      }
      //病原体が0で、治療薬が作成されている時
      if( cnt[i] == 0 && gameStatus.cureMarkersFlag[i] == 1 ){
        gameStatus.cureMarkersFlag[i] = 2;
      }
    }
    
    ////知識の共有
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
        //対象のカードを持っているのが他のプレイヤーの場合、自分だけを対象にする
        if( shareKnowledgeSrc != p.no ){
          draws.displayPlayer.clear();
          draws.displayPlayer.append( p.no );
        }//if
        gameStatus.pressFlag = false;
        phase = PHASE.SHARE_KNOWLEDGE;
      }//if
    }//if
    
    ////治療薬の発見
    if( p.role == ROLES.SCIENTIST ){
      gameStatus.discoverCureCnt = 4;  //科学者は4枚で治療薬を作成可能
    }else{
      gameStatus.discoverCureCnt = 5;  //他は5枚で治療薬を作成可能
    }
    if( playerActionImgSet.get( 3 ).isHit() & p.cards.size() >= gameStatus.discoverCureCnt && searchList( gameStatus.researchStationList, p.position ) ){
      phase = PHASE.DISCOVER_A_CURE;
      draws.targetPlayerNo = MAIN_TURN.n;
      gameStatus.pressFlag = false;
    }
    
    ////イベントカードの使用
    if( playerActionImgSet.get( 4 ).isHit() && searchList( p.cards, 47, 48, 49, 50, 51 ) ){
      phase = PHASE.EVENT_CARD;
      gameStatus.pressFlag = false;
      draws.targetPlayerNo = MAIN_TURN.n;
    }
    
    ////特殊技能の発動
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
    
    ////ヘルプ
    if( rectHit( width - RECT_SIZE * 0.5, 0, RECT_SIZE * 0.5, RECT_SIZE * 0.5 ) ){
      gameStatus.helpFlag = !gameStatus.helpFlag;
    }
  }//gameMainPhase
    
  void gameInputPhase(){
    int i,j;
    
    switch( phase ){
      case GAME_DIFFICULTY_INPUT:  //エピデミックカードの枚数の設定
        if( 51 < key && key < 55 ){ // key -> '4' to '6'
          epidemicCardNumber = int(key) - 48;
          phase = PHASE.PLAYER_NUMBER_INPUT;
        }
      break;  //GAME_DIFFICULTY_INPUT
      case PLAYER_NUMBER_INPUT:  //プレイヤー人数の設定
        if( 49 < key && key < 53 ){ // key -> '2' to '4'
          playerNumber = int(key) - 48;
          phase = PHASE.PLAYER_NAME_INPUT;
        }
      break;  //PLAYER_NUMBER
      case PLAYER_NAME_INPUT:    //プレイヤーの名前の設定
        if( keyCode == BACKSPACE ){  //1文字削除
          if( charLst.size() > 0 ){
            charLst.remove( charLst.size() - 1 );
          }
          return;
        }
        if( keyCode != SHIFT && keyCode != RETURN && keyCode != ENTER ){  //1文字追加
          String k = ""; k += key;
          charLst.append( k );
          return;
        }
        
        if( ( keyCode == RETURN || keyCode == ENTER ) && charLst.size() >= 1 ){  //決定
          String str = "";
          for( i = 0; i < charLst.size(); i++ ){
            str += charLst.get( i );
          }
          playerNames.append( str );
          charLst.clear();
          if( playerNames.size() >= playerNumber ){
            int rnd, rndMaxNum = 7;
            //           危機管理官                 通信指令員             作戦エキスパート   衛生兵       科学者          研究員           検疫官
            ROLES[] r = {ROLES.CONTINGENCY_PLANNER,ROLES.DISPATCHER,ROLES.OPERATIONS_EXPERT,ROLES.MEDIC,ROLES.SCIENTIST,ROLES.RESEARCHER,ROLES.QUARANTINE_SPECIALIST};
            TEXT_SIZE = bs.y * 0.125;  //テキストサイズ再設定
            textSize( TEXT_SIZE );
            players.clear();
            for( i = 0; i < playerNumber; i++ ){
              rnd = floor( random( 0, rndMaxNum ) );
              players.add( new player( playerNames.get( i ), 12, todoufuken.get( 12 ).x, todoufuken.get( 12 ).y, i, r[rnd] ) );  //プレイヤーの名前と位置設定
              r[rnd] = r[--rndMaxNum];
            }
            
            //プレイヤーカードの設定
            IntList pDeck = new IntList();
            
            //0 to 51 ランダムに設定
            pDeck = setList( todoufukenNum + eventCardNum );
            
            //プレイヤー人数によって初期のカードを配る枚数を変更
            int playerCardNum = 0;
            switch( playerNumber ){
              case 2:  playerCardNum = 4;  break;
              case 3:  playerCardNum = 3;  break;
              case 4:  playerCardNum = 2;  break;
            }
            
            //プレイヤーにカードをセット
            for( player p : players ){
              for( i = 0; i < playerCardNum; i++ ){
                p.setCard( pDeck.remove( pDeck.size() - 1 ) );
              }
            }
            
            //プレイヤーデッキにエピデミックカードを設定
            IntList rndNum = new IntList();
            int cnt = floor( pDeck.size() / epidemicCardNumber );  //プレイヤーデッキの残りをエピデックカード枚数で分割した数
            int amari = pDeck.size() % epidemicCardNumber;         //↑の余り
            
            for( i = 0; i < epidemicCardNumber; i++ ){             //エピデックカード枚数回ループ
              if( i == epidemicCardNumber - 1 ){                   //最後のループだけ余りを足す
                cnt += amari;
              }
              rndNum.append( i + 52 );                             //エピデミックカードをセット 52 to 57
              for( j = 0; j < cnt; j++ ){                          //(分割数 + エピデミックカード1つ)回ループ
                rndNum.append( pDeck.remove( pDeck.size() - 1 ) ); //変数に分割した数を入れる
              }
              rndNum.shuffle();                                    //シャッフル
              for( j = 0; j < rndNum.size(); j++ ){                //(分割数 + エピデミックカード1つ)回ループ
                gameStatus.playerDeck.append( rndNum.get( j ) );   //シャッフルした値をデッキにセット
              }
              rndNum.clear();                                      //変数をクリア
            }
            
            //感染カードの設定
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
            phase = PHASE.GAME;  //フェイズチェンジ
          }//if
        }//if
      break;  //PLAYER_NAME_INPUT
    }
  }
}