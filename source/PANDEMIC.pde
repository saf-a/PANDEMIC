void setup(){
  
  fullScreen();
  //size(displayWidth,displayHeight);
  //size(1200,1000);
  frameRate( 60 );
  
  textFont( createFont( "MS Gothic",48,true ) ); //フォント指定
  
  draws = new Draws();
  phases = new Phases();
  gameStatus = new GAME_STATUS();  
    
  //変数のセットアップ
  setupVar();
  
  textAlign( CENTER, CENTER );
  textSize( TEXT_SIZE );
  ellipseMode( CENTER );
  noSmooth();
  strokeWeight(1);
}

void draw(){
  drawBackground();
  
  //アクション4回でターンチェンジ
  if( ACTION_COUNT == 0 && phase != PHASE.HAND_LIMIT ){
    turnChange();
  }
  
  //ヘルプの表示
  if( phase == PHASE.GAME ){
    draws.helpDraw();
  }
  gameStatus.update();
  //line( mouseX, 0, mouseX, height );
  //line( 0, mouseY, width, mouseY );
}

void keyPressed(){
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

void mouseReleased(){
  gameStatus.pressFlag = true;
}

void mousePressed(){
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

void drawBackground(){
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