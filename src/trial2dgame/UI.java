package trial2dgame;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import entity.Entity;
import object.OBJ_Coin;
import quiz.Question;
import quiz.QuestionLoader;
import java.util.List;
import java.util.Random;

public class UI {
	
	GamePanel gp;
	Graphics2D g2;
	public Font maruMonica;
	Font poynterText;
	BufferedImage coin;
	public boolean messageOn = false;
	ArrayList<String> message = new ArrayList<>();
	ArrayList<Integer> messageCounter = new ArrayList<>();
	
	public boolean gameFinished = false;
	public boolean dialogueActive = false;
	public String currentDialogue = "";
	public int dialogueIndex = 0;
	public int dialogueSet = 0;
	
	public int commandNum = 0;
	public int playerSlotCol = 0;
	public int playerSlotRow = 0;
	public int npcSlotCol = 0;
	public int npcSlotRow = 0;
	int subState = 0;
	int counter = 0; // for transition
	public Entity npc;
	public boolean isBuying = false;
	private Random random = new Random();

    // Quiz/Battle feedback
    public String quizFeedback = ""; // "" means no feedback
    public long quizFeedbackTime = 0;
    public static final int FEEDBACK_DURATION = 2600; // milliseconds

    // Monster slain notification state
    public boolean monsterSlainNotification = false;
    public long monsterSlainTime = 0;
    public static final int MONSTER_SLAIN_DISPLAY_TIME = 1400; // ms

    // Quiz state
    public List<Question> currentQuiz;
    public int quizIndex = 0;
    public Object gameState;
    public Object battleState;

    // For battle UI rendering
    public BufferedImage playerBattleImage; // assign these from your player/monster art!
    public BufferedImage monsterBattleImage;
    public Entity currentBattleMonster;
	
	public boolean showSaving = false;
	public boolean showLoading = false;
	private long saveLoadMessageTime = 0;
	private final long SAVELOAD_MESSAGE_DURATION = 2000; // milliseconds
	
	public UI(GamePanel gp) {
		this.gp = gp;
		currentQuiz = QuestionLoader.loadQuestions("chap1", "easy"); // ADDED-ELLA
		
        try {
            InputStream is = getClass().getResourceAsStream("/font/x12y16pxMaruMonica.ttf");
            maruMonica = Font.createFont(Font.TRUETYPE_FONT, is);
            is = getClass().getResourceAsStream("/font/PoynterText Regular.ttf");
            poynterText = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
		coin = new OBJ_Coin(gp).down1;
		
	}
	public void addMessage(String text) {

		message.add(text);
		messageCounter.add(0);
	}
	
	// ==== Battle/Quiz UI (with player and monster highlight boxes and glow, and 2D HP bar text) ====
    public void drawQuizBattle() {
        g2.setColor(new Color(0, 0, 0, 220));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        int spriteSize = gp.tileSize * 3;
        int boxWidth = spriteSize + 32;
        int boxHeight = spriteSize + 48;
        int boxArc = 32;

        int playerBoxX = gp.tileSize * 2;
        int playerBoxY = gp.tileSize * 2 + 40;

        int monsterBoxX = gp.screenWidth - gp.tileSize * 2 - boxWidth;
        int monsterBoxY = gp.tileSize * 2 + 40;

        drawGlowBox(g2, playerBoxX, playerBoxY, boxWidth, boxHeight, boxArc, new Color(0, 255, 160, 120), true);
        drawGlowBox(g2, monsterBoxX, monsterBoxY, boxWidth, boxHeight, boxArc, new Color(255, 90, 0, 120), true);

        BufferedImage playerImg = (playerBattleImage != null) ? playerBattleImage : gp.player.down1;
        BufferedImage monsterImg = (monsterBattleImage != null) ? monsterBattleImage : getCurrentBattleMonsterImage();
        int playerImgX = playerBoxX + (boxWidth - spriteSize) / 2;
        int playerImgY = playerBoxY + (boxHeight - spriteSize) / 2 + 10;
        g2.drawImage(playerImg, playerImgX, playerImgY, spriteSize, spriteSize, null);

        int monsterImgX = monsterBoxX + (boxWidth - spriteSize) / 2;
        int monsterImgY = monsterBoxY + (boxHeight - spriteSize) / 2 + 10;
        g2.drawImage(monsterImg, monsterImgX, monsterImgY, spriteSize, spriteSize, null);

        int hpBarYOffset = 38; // more space between hp bar and sprite box
        drawPlayerLifeBar2D(playerBoxX, playerBoxY - hpBarYOffset, boxWidth, gp.player.life, gp.player.getMaxLife());

        Entity monster = getCurrentBattleMonster();
        if (monster != null) {
            drawMonsterLifeBar2D(monsterBoxX, monsterBoxY - hpBarYOffset, boxWidth, monster.life, monster.maxLife, monster.name);
        }

        // Monster Slain Notification with long, gentle fade and play-state background
        if (monsterSlainNotification) {
            gp.drawGameWorld(g2); // Draw play state as background
            Graphics2D g2d = (Graphics2D) g2.create();
            GradientPaint gpnt = new GradientPaint(
                0, 0, new Color(0,0,0, 180),
                0, gp.screenHeight, new Color(0,0,0, 120)
            );
            g2d.setPaint(gpnt);
            g2d.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
            g2d.dispose();

            String notify = "You have slain the monster";
            float elapsed = (System.currentTimeMillis() - monsterSlainTime) / (float) MONSTER_SLAIN_DISPLAY_TIME;
            elapsed = Math.min(elapsed, 1.0f);

            float scale = 0.8f + 0.5f * (float)Math.sin(Math.PI * Math.min(elapsed, 1.0));
            int alpha = (int)(255 * Math.min(elapsed * 1.25f, 1.0f));

            Font notifFont = maruMonica != null ? maruMonica.deriveFont(Font.BOLD, 44f * scale) : new Font("Arial", Font.BOLD, (int)(44 * scale));
            g2.setFont(notifFont);
            int textWidth = g2.getFontMetrics().stringWidth(notify);
            int textX = (gp.screenWidth - textWidth) / 2;
            int textY = gp.screenHeight / 2;

            for (int glow = 8; glow > 0; --glow) {
                g2.setColor(new Color(255, 230, 80, Math.max(0, Math.min(60, alpha / 3))));
                g2.drawString(notify, textX + glow, textY + glow);
            }
            g2.setColor(new Color(255, 230, 80, alpha));
            g2.drawString(notify, textX, textY);

            if (System.currentTimeMillis() - monsterSlainTime > MONSTER_SLAIN_DISPLAY_TIME) {
                monsterSlainNotification = false;
                gp.gameState = gp.playState;
            }
            return;
        }

        if (quizFeedback != null && !quizFeedback.isEmpty() &&
                System.currentTimeMillis() - quizFeedbackTime < FEEDBACK_DURATION) {
            g2.setFont(maruMonica.deriveFont(Font.BOLD, 36f));
            g2.setColor("Correct!".equals(quizFeedback) ? Color.GREEN : Color.RED);
            String msg = quizFeedback;
            int msgWidth = g2.getFontMetrics().stringWidth(msg);
            int x = (gp.screenWidth - msgWidth) / 2;
            int y = playerBoxY + boxHeight + 25;
            g2.drawString(msg, x, y);
        } else if (quizFeedback != null && !quizFeedback.isEmpty()) {
            quizFeedback = "";
        }

        drawQuizPanel();
    }

    // Helper for glow effect and border, inside UI.java
    private void drawGlowBox(Graphics2D g2, int x, int y, int w, int h, int arc, Color glowColor, boolean whiteBorder) {
        // Outer glow
        for (int glow = 16; glow > 0; glow -= 5) {
            g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 10 * glow / 5));
            g2.fillRoundRect(x - glow, y - glow, w + glow * 2, h + glow * 2, arc + glow * 2, arc + glow * 2);
        }
        // White inner border
        if (whiteBorder) {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(4));
            g2.drawRoundRect(x, y, w, h, arc, arc);
        }
        // Fill box background (black, but allow sprite to glow on top)
        g2.setColor(new Color(0, 0, 0, 220));
        g2.fillRoundRect(x + 4, y + 4, w - 8, h - 8, arc - 4, arc - 4);
    }

    // --- 2D/arcade style HP bars with glow and custom font ---
    private void drawPlayerLifeBar2D(int x, int y, int width, int life, int maxLife) {
        int barHeight = 20;
        int filledWidth = (int) ((double) life / gp.player.getMaxLife() * (width - 4));

        // Glow
        for (int glow = 6; glow > 0; glow -= 3) {
            g2.setColor(new Color(0, 255, 160, 35 * glow / 3));
            g2.fillRoundRect(x - glow, y - glow, width + glow * 2, barHeight + glow * 2, 16 + glow * 2, 16 + glow * 2);
        }

        // Bar bg and value
        g2.setColor(Color.DARK_GRAY);
        g2.fillRoundRect(x + 2, y, width - 4, barHeight, 12, 12);
        g2.setColor(new Color(0, 255, 60));
        g2.fillRoundRect(x + 2, y, filledWidth, barHeight, 12, 12);

        // White border
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(x + 2, y, width - 4, barHeight, 12, 12);

        // 2D pixel font (fallback to Arial Bold if not found)
        Font hpFont = maruMonica != null ? maruMonica.deriveFont(Font.BOLD, 18f) : new Font("maruMonica", Font.BOLD, 18);
        g2.setFont(hpFont);
        // Glow text
        g2.setColor(new Color(0,255,160,170));
        g2.drawString("PLAYER", x + 2+1, y - 7+1);
        // Main text
        g2.setColor(Color.WHITE);
        g2.drawString("PLAYER", x + 2, y - 7);
    }

    private void drawMonsterLifeBar2D(int x, int y, int width, int life, int maxLife, String monsterName) {
        int barHeight = 20;
        int filledWidth = (int) ((double) life / maxLife * (width - 4));

        // Glow
        for (int glow = 6; glow > 0; glow -= 3) {
            g2.setColor(new Color(255, 90, 0, 35 * glow / 3));
            g2.fillRoundRect(x - glow, y - glow, width + glow * 2, barHeight + glow * 2, 16 + glow * 2, 16 + glow * 2);
        }

        // Bar bg and value
        g2.setColor(Color.DARK_GRAY);
        g2.fillRoundRect(x + 2, y, width - 4, barHeight, 12, 12);
        g2.setColor(new Color(255, 30, 30));
        g2.fillRoundRect(x + 2, y, filledWidth, barHeight, 12, 12);

        // White border
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(x + 2, y, width - 4, barHeight, 12, 12);

        Font hpFont = maruMonica != null ? maruMonica.deriveFont(Font.BOLD, 17f) : new Font("maruMonica", Font.BOLD, 18);
        g2.setFont(hpFont);
        // Glow text
        g2.setColor(new Color(255,90,0,170));
        g2.drawString(monsterName != null ? monsterName : "MONSTER", x + 2+1, y - 7+1);
        // Main text
        g2.setColor(Color.WHITE);
        g2.drawString(monsterName != null ? monsterName : "MONSTER", x + 2, y - 7);
    }

    private Entity getCurrentBattleMonster() {
        if (currentBattleMonster != null) return currentBattleMonster;
        for (Entity m : gp.monster[gp.currentMap]) {
            if (m != null && m.alive && !m.dying) return m;
        }
        return null;
    }
    private BufferedImage getCurrentBattleMonsterImage() {
        Entity m = getCurrentBattleMonster();
        return (m != null && m.down1 != null) ? m.down1 : gp.player.down1;
    }

    // --- Bottom panel for quiz question + choices (maruMonica font + glow) ---
    private void drawQuizPanel() {
        if (currentQuiz == null || quizIndex >= currentQuiz.size()) return;
        Question q = currentQuiz.get(quizIndex);

        // Panel settings
        int panelMargin = 16;
        int panelHeight = gp.tileSize * 4;
        int panelY = gp.screenHeight - panelHeight - panelMargin;
        int panelWidth = gp.screenWidth - panelMargin * 2;
        int panelX = panelMargin;
        int borderThickness = 4;

        // Draw solid black panel with white border
        g2.setColor(Color.WHITE);
        g2.fillRect(panelX - borderThickness, panelY - borderThickness, panelWidth + borderThickness * 2, panelHeight + borderThickness * 2);
        g2.setColor(Color.BLACK);
        g2.fillRect(panelX, panelY, panelWidth, panelHeight);

        // --- Question: maruMonica, centered, white, with glow ---
        String question = q.getQuestion();
        int questionBoxWidth = panelWidth - 40;
        int maxQuestionFontSize = 40;
        int minQuestionFontSize = 22;
        int fontSize = maxQuestionFontSize;
        Font questionFont;
        int strWidth;

        do {
            questionFont = maruMonica.deriveFont(Font.BOLD, (float)fontSize);
            g2.setFont(questionFont);
            strWidth = g2.getFontMetrics().stringWidth(question);
            fontSize--;
        } while (strWidth > questionBoxWidth && fontSize > minQuestionFontSize);

        int questionY = panelY + 56;

        // Glow
        for (int glow = 6; glow > 0; --glow) {
            g2.setColor(new Color(0, 180, 255, 30));
            g2.setFont(questionFont);
            g2.drawString(question, panelX + (panelWidth - strWidth) / 2 + glow, questionY + glow);
        }
        // Main text
        g2.setFont(questionFont);
        g2.setColor(Color.WHITE);
        g2.drawString(question, panelX + (panelWidth - strWidth) / 2, questionY);

        // --- Choices: maruMonica, centered, white/black, with glow on text ---
        int numChoices = q.getChoices().size();
        int choicePanelPadding = 48;
        int choiceAreaWidth = panelWidth - 2 * choicePanelPadding;
        int spacing = 24;
        int choiceBoxWidth = (choiceAreaWidth - (numChoices - 1) * spacing) / numChoices;
        int choiceBoxHeight = 48;
        int startX = panelX + choicePanelPadding;
        int choiceY = panelY + panelHeight - choiceBoxHeight - 38;

        int maxChoiceFont = 22, minChoiceFont = 12;

        for (int i = 0; i < numChoices; i++) {
            String choice = q.getChoices().get(i);

            int boxX = startX + i * (choiceBoxWidth + spacing);
            int boxY = choiceY;

            // Fill box fully inside the panel
            g2.setColor(i == commandNum ? Color.WHITE : Color.BLACK);
            g2.fillRect(boxX, boxY, choiceBoxWidth, choiceBoxHeight);

            // Border for all choices, thicker and black for selected, fully inside the panel
            g2.setStroke(new BasicStroke(i == commandNum ? 3 : 2));
            g2.setColor(Color.WHITE);
            g2.drawRect(boxX, boxY, choiceBoxWidth, choiceBoxHeight);

            // Border for selected (black, slightly inset)
            if (i == commandNum) {
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(boxX + 2, boxY + 2, choiceBoxWidth - 4, choiceBoxHeight - 4);
            }

            // Dynamically size font for choices so text fits nicely within each box
            int cFontSz = maxChoiceFont;
            Font cFont;
            int cTextWidth;
            do {
                cFont = maruMonica.deriveFont(Font.BOLD, (float)cFontSz);
                g2.setFont(cFont);
                cTextWidth = g2.getFontMetrics().stringWidth(choice);
                cFontSz--;
            } while (cTextWidth > choiceBoxWidth - 16 && cFontSz > minChoiceFont);

            int textX = boxX + (choiceBoxWidth - cTextWidth) / 2;
            int textY = boxY + (choiceBoxHeight + g2.getFontMetrics().getAscent()) / 2 - 6;

            // Glow effect for choices text
            for (int glow = 4; glow > 0; --glow) {
                g2.setColor(i == commandNum ? new Color(0, 180, 255, 60) : new Color(0, 180, 255, 35));
                g2.setFont(cFont);
                g2.drawString(choice, textX + glow, textY + glow);
            }
            // Main text
            g2.setColor(i == commandNum ? Color.BLACK : Color.WHITE);
            g2.setFont(cFont);
            g2.drawString(choice, textX, textY);
        }

        // Instructions, centered below panel (optional: can give glow too if you want)
        g2.setFont(maruMonica.deriveFont(Font.ITALIC, 15f));
        String instr = "Use ←W S→ and press ENTER to answer";
        int instrWidth = g2.getFontMetrics().stringWidth(instr);
        int instrY = panelY + panelHeight + 28;
        for (int glow = 3; glow > 0; --glow) {
            g2.setColor(new Color(0, 180, 255, 25));
            g2.drawString(instr, (gp.screenWidth - instrWidth) / 2 + glow, instrY + glow);
        }
        g2.setColor(Color.WHITE);
        g2.drawString(instr, (gp.screenWidth - instrWidth) / 2, instrY);
    }

    public List<String> wrapText(String text, int maxWidth, Graphics2D g2) {
        List<String> lines = new ArrayList<>();
        FontMetrics fm = g2.getFontMetrics();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String testLine = line + word + " ";
            if (fm.stringWidth(testLine) > maxWidth) {
                lines.add(line.toString());
                line = new StringBuilder(word + " ");
            } else {
                line.append(word).append(" ");
            }
        }
        lines.add(line.toString());
        return lines;
    }

 // Handles answer submission and HP logic for quiz battles, and shows feedback on screen
    public void handleQuizAnswer(int selectedChoiceIndex) {
        if (currentQuiz == null || quizIndex >= currentQuiz.size()) return;
        Question q = currentQuiz.get(quizIndex);

        String playerAnswer = q.getChoices().get(selectedChoiceIndex).substring(0, 1);
        String correctAnswer = q.getAnswer();

        Entity monster = getCurrentBattleMonster();

        boolean isCorrect = playerAnswer.equalsIgnoreCase(correctAnswer);
        quizFeedback = isCorrect ? "Correct!" : "Wrong!";
        quizFeedbackTime = System.currentTimeMillis();

        if (isCorrect) {
            if (monster != null) {
            	
            	int damage = gp.player.calculateBattleDamage(monster);
                monster.life -= damage;
                
                addMessage("Correct! Monster HP -" + damage);
                gp.playSE(5);
                if (monster.life <= 0) {
                    monster.life = 0;
                    monster.dying = true;
                    addMessage("Monster defeated!");

                    // === DURABILITY LOGIC after defeating monster ===
                    // Decrease currentWeapon
                    if (gp.player.currentWeapon != null) {
                        gp.player.currentWeapon.durability--;
                        addMessage("[Weapon] Durability: " + (gp.player.currentWeapon.durability >= 0 ? gp.player.currentWeapon.durability : 0));
                        if (gp.player.currentWeapon.durability <= 0) {
                            addMessage("Your weapon broke!");
                            gp.player.inventory.remove(gp.player.currentWeapon);
                            gp.player.currentWeapon = null;
                        }
                    }
                    // Decrease currentShield
                    if (gp.player.currentShield != null) {
                        gp.player.currentShield.durability--;
                        addMessage("[Shield] Durability: " + (gp.player.currentShield.durability >= 0 ? gp.player.currentShield.durability : 0));
                        if (gp.player.currentShield.durability <= 0) {
                            addMessage("Your shield broke!");
                            gp.player.inventory.remove(gp.player.currentShield);
                            gp.player.currentShield = null;
                        }
                    }
                    // === END DURABILITY LOGIC ===

                    monsterSlainNotification = true;
                    monsterSlainTime = System.currentTimeMillis();
                    return;
                }
            }
        } else {
            // Use the current monster's attack value safely:
            if (monster != null) {
                gp.player.life -= monster.attack;
                addMessage("Wrong! Player HP -" + monster.attack);
                gp.playSE(6);
                if (gp.player.life <= 0) {
                    gp.player.life = 0;
                    gp.gameState = gp.gameOverState;
                    addMessage("You have been defeated!");
                    return;
                }
            }
        }

        // --- NEW: Loop questions randomly as long as player and monster are alive ---
        if ((gp.player.life > 0) && (monster != null && monster.life > 0)) {
            // Option 1: Random question each time (can repeat)
            quizIndex = random.nextInt(currentQuiz.size());
            // Option 2: Cycle through questions in order (loop back)
            // quizIndex = (quizIndex + 1) % currentQuiz.size();
        } else {
            // End battle if one side is defeated
            gp.gameState = gp.playState;
            addMessage("Quiz ended!");
        }
    }

		
	public void draw(Graphics2D g2) {
		
		this.g2 = g2;
		
		g2.setFont(maruMonica);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setColor(Color.white);
		
		//TITLE STATE
		if (gp.gameState == gp.titleState) {
			drawTitleScreen();
		}	
		//PLAY STATE
		if (gp.gameState == gp.playState) {
			drawPlayerLife();
			drawMessage();			
		}	
		//PAUSE STATE
		if (gp.gameState == gp.pauseState) {
			drawPlayerLife();
			drawPauseScreen();
		}		
		//DIALOGUE STATE
		if (gp.gameState == gp.dialogueState) {
			drawPlayerLife();
			drawDialogueScreen();
		}	
		//CHARACTER STATE
		if (gp.gameState == gp.characterState) {
			drawCharacterScreen();
			drawInventory(gp.player, true);
		}
		//OPTIONS STATE
		if (gp.gameState == gp.optionsState) {
			drawOptionsScreen();
		}
		//GAMEOVER STATE
		if (gp.gameState == gp.gameOverState) {
			drawGameOverScreen();
		}
		//TRANSITION STATE
		if (gp.gameState == gp.transitionState) {
			drawTransition();
		}
		//TRADE STATE
		if (gp.gameState == gp.tradeState) {
			drawTradeScreen();
		}
		//BATTLE STATE ADDED-ELLA
		if (gp.gameState == gp.battleState) {
			drawQuizBattle();
		}
		
	}
	public void drawPlayerLife() {
		
	    int offsetX = 40; // Move further to the right
	    int x = gp.tileSize / 2 + offsetX;
	    int y = gp.tileSize / 2;

	    int currentLife = Math.max(gp.player.life, 0);

	    int barWidth = gp.tileSize * 8;   // Much wider (e.g., 8 tiles)
	    int barHeight = 24;               // Taller HP bar

	    double oneScale = (double) barWidth / gp.player.getMaxLife();
	    double hpBarValue = oneScale * currentLife;

	    g2.setFont(g2.getFont().deriveFont(Font.BOLD, 22F)); // Larger font
	    g2.setColor(Color.white);
	    g2.drawString("HP", x - 30, y + 18); // Adjusted for new font and bar size

	    g2.setColor(new Color(35, 35, 35)); // Dark background
	    g2.fillRoundRect(x - 1, y - 1, barWidth + 2, barHeight + 2, 14, 14);

	    g2.setColor(new Color(0, 200, 0)); // Slightly darker green
	    g2.fillRoundRect(x, y, (int) hpBarValue, barHeight, 14, 14);

	    String hpStatus = currentLife + " / " + gp.player.getMaxLife();
	    int textX = x + (barWidth / 2) - g2.getFontMetrics().stringWidth(hpStatus) / 2;
	    int textY = y + barHeight - 5;
	    g2.setColor(Color.white);
	    g2.drawString(hpStatus, textX, textY);
		
	}
	public void drawMessage() {
		
		int messageX = gp.tileSize;
		int messageY = gp.tileSize*4;
		g2.setFont(g2.getFont().deriveFont(Font.BOLD, 25F));
		
		for(int i = 0; i < message.size(); i++) {
			
			if(message.get(i) != null) {
				
				g2.setColor(Color.black);
				g2.drawString(message.get(i), messageX+2, messageY+2);
				g2.setColor(Color.white);
				g2.drawString(message.get(i), messageX, messageY);
				
				int counter = messageCounter.get(i) + 1; //messageCounter++
				messageCounter.set(i, counter); //set the counter to the array
				messageY += 50;
				
				if(messageCounter.get(i) > 180) {
					message.remove(i);
					messageCounter.remove(i);
				}
			
			}
		}
		
		
	}
	public void drawTitleScreen() {

		g2.setColor(Color.decode("#FFB3DE"));
		g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

		// TITLE NAME
		g2.setFont(g2.getFont().deriveFont(Font.BOLD, 95F));
		String text = "Java Realms";
		int y = gp.tileSize * 4;

		for (String line : text.split("\n")) {
			int x = getXforCenteredText(line);

		    // SHADOW
		    g2.setColor(Color.decode("#802B00"));
		    g2.drawString(line, x + 4, y + 4);

		    // MAIN COLOR
		    g2.setColor(Color.white);
		    g2.drawString(line, x, y);

		    y += gp.tileSize * 2;    
		 }
		
		// CAT IMAGE
		int NWidth = gp.tileSize * 2; //size for drawing, dimensions of displayed image
	    int NHeight = gp.tileSize * 2; //both these -v
	    //define how large the image (gp.player.down1) will appear when drawn to the screen
	    int NX = gp.screenWidth / 2 - NWidth / 2;
	    int NY = y - gp.tileSize;
	    g2.drawImage(gp.player.down1, NX, NY, NWidth, NHeight, null);    
	
	    //MENU
		g2.setFont(g2.getFont().deriveFont(Font.BOLD, 48F));

		text = "NEW GAME";
		int x = getXforCenteredText(text);
		y += gp.tileSize * 2; 
	    g2.drawString(text, x, y);
	    if(commandNum == 0) {
	    	g2.drawString(">", x-gp.tileSize, y);
	    }
	    
	    text = "LOAD GAME";
		x = getXforCenteredText(text);
		y += gp.tileSize; 
	    g2.drawString(text, x, y);
	    if(commandNum == 1) {
	    	g2.drawString(">", x-gp.tileSize, y);
	    }
	    
	    text = "QUIT";
		x = getXforCenteredText(text);
		y += gp.tileSize; 
	    g2.drawString(text, x, y);
	    if(commandNum == 2) {
	    	g2.drawString(">", x-gp.tileSize, y);
	    }
	}	
	public void drawPauseScreen() {
			
		g2.setFont(g2.getFont().deriveFont(Font.PLAIN,80F));
		String text = "PAUSED";
		int x = getXforCenteredText(text);
		int y = (gp.screenHeight/2) + gp.tileSize/2;
		
		g2.drawString(text, x, y);
		
	}
	public void drawDialogueScreen() {	// ALSO PRESENT IN TRADE_SELECT
		
		// WINDOW
		int x = gp.tileSize*2;
		int y = gp.tileSize/2;
		int width = gp.screenWidth - (gp.tileSize*4);
		int height = gp.tileSize*4;
		drawSubWindow(x, y, width, height);
		
		g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 32F));
		x += gp.tileSize;
		y += gp.tileSize;
		

		if(npc.dialogues[npc.dialogueSet][npc.dialogueIndex] != null) {
			
			currentDialogue = npc.dialogues[npc.dialogueSet][npc.dialogueIndex];		
			
			// Check if Enter was just pressed
	        if (gp.keyH.enterPressed) {
	            System.out.println("Enter pressed! Current dialogue index: " + npc.dialogueIndex);

	            npc.dialogueIndex++;
	            gp.keyH.enterPressed = false; // Reset the key press flag
	        }
		}
		else { 	// IF NO TEXT IS IN THE ARRAY
			npc.dialogueIndex = 0;
			
			if(gp.gameState == gp.dialogueState) {
				gp.gameState = gp.playState;
			}
		}
		
		for(String line : currentDialogue.split("\n")) {
			g2.drawString(line, x, y);
			y += 40;
		}
	}
	public void drawCharacterScreen() {
		
		// CREATE A FRAME
		final int frameX = gp.tileSize;
		final int frameY = gp.tileSize;
		final int frameWidth = gp.tileSize*5;
		final int frameHeight = gp.tileSize*10;
		drawSubWindow(frameX, frameY, frameWidth, frameHeight);
		
		// TEXT
		g2.setColor(Color.white);
		g2.setFont(g2.getFont().deriveFont(35F));
		
		int textX = frameX + 20;
		int textY = frameY + gp.tileSize;
		final int lineHeight = 38;
	
		// NAME
		g2.drawString("Level", textX, textY);
		textY += lineHeight;
		g2.drawString("Life", textX, textY);
		textY += lineHeight;
		g2.drawString("Attack", textX, textY);
		textY += lineHeight;
		g2.drawString("Defense", textX, textY);
		textY += lineHeight;
		g2.drawString("Knowledge", textX, textY);
		textY += lineHeight;
		g2.drawString("Exp", textX, textY);
		textY += lineHeight;
		g2.drawString("Next Level", textX, textY);
		textY += lineHeight;
		g2.drawString("Coin", textX, textY);
		textY += lineHeight + 20;
		g2.drawString("Weapon", textX, textY);
		textY += lineHeight + 15;
		g2.drawString("Shield", textX, textY);
		textY += lineHeight;
		
		// VALUES
		int tailX = (frameX + frameWidth) - 30;
		
		// Reset textY
		textY = frameY + gp.tileSize;
		String value;
		
		value = String.valueOf(gp.player.level);
		textX = getXforAlignToRightText(value, tailX);
		g2.drawString(value, textX, textY);
		textY += lineHeight;
		
		value = String.valueOf(gp.player.life + "/" + gp.player.getMaxLife());
		textX = getXforAlignToRightText(value, tailX);
		g2.drawString(value, textX, textY);
		textY += lineHeight;
		
		value = String.valueOf(gp.player.getAttack());
		textX = getXforAlignToRightText(value, tailX);
		g2.drawString(value, textX, textY);
		textY += lineHeight;
		
		value = String.valueOf(gp.player.getDefense());
		textX = getXforAlignToRightText(value, tailX);
		g2.drawString(value, textX, textY);
		textY += lineHeight;
		
        value = String.valueOf(gp.player.getKnowledge());
		textX = getXforAlignToRightText(value, tailX);
		g2.drawString(value, textX, textY);
		textY += lineHeight;
		
		value = String.valueOf(gp.player.exp);
		textX = getXforAlignToRightText(value, tailX);
		g2.drawString(value, textX, textY);
		textY += lineHeight;
		
		value = String.valueOf(gp.player.nextLevelExp);
		textX = getXforAlignToRightText(value, tailX);
		g2.drawString(value, textX, textY);
		textY += lineHeight;
		
		value = String.valueOf(gp.player.coin);
		textX = getXforAlignToRightText(value, tailX);
		g2.drawString(value, textX, textY);
		textY += lineHeight;
		
		if (gp.player.currentWeapon != null && gp.player.currentWeapon.down1 != null) {
            g2.drawImage(gp.player.currentWeapon.down1, tailX - gp.tileSize, textY-14, null);
        }		
		textY += gp.tileSize;
		if (gp.player.currentShield != null && gp.player.currentShield.down1 != null) {
            g2.drawImage(gp.player.currentShield.down1, tailX - gp.tileSize, textY-14, null);
        }
	
	}
	public void drawInventory(Entity entity, boolean cursor) {
		
		int frameX = 0;
		int frameY = 0;
		int frameWidth = 0;
		int frameHeight = 0;
		int slotCol = 0;
		int slotRow = 0;
		
		if(entity == gp.player) {
			// FRAME
			frameX = gp.tileSize*9;
			frameY = gp.tileSize;
			frameWidth = gp.tileSize*6;
			frameHeight = gp.tileSize*5;
			slotCol = playerSlotCol;
			slotRow = playerSlotRow;
		}
		else {
			// FRAME
			frameX = gp.tileSize;
			frameY = gp.tileSize;
			frameWidth = gp.tileSize*6;
			frameHeight = gp.tileSize*5;
			slotCol = npcSlotCol;
			slotRow = npcSlotRow;
		}
		
		// FRAME
		drawSubWindow(frameX, frameY, frameWidth, frameHeight);
		
		// SLOT
		final int slotXstart = frameX + 20;
		final int slotYstart = frameY + 20;
		int slotX = slotXstart;
		int slotY = slotYstart;
		int slotSize = gp.tileSize + 3;
		
		// DRAW PLAYER'S/NPC'S ITEMS
		for(int i = 0; i < entity.inventory.size(); i++) {
			
			// EQUIP CURSOR
			if(entity.inventory.get(i) == entity.currentWeapon ||
			   entity.inventory.get(i) == entity.currentShield ||
			   entity.inventory.get(i) == entity.currentLight) {
				
				g2.setColor(new Color(240,190,90));
				g2.fillRoundRect(slotX, slotY, gp.tileSize, gp.tileSize, 10, 10);
			}
			
			g2.drawImage(entity.inventory.get(i).down1, slotX, slotY, null);
		
			// DISPLAY AMOUNT
			if(entity == gp.player && entity.inventory.get(i).amount > 1) {
				
				g2.setFont(g2.getFont().deriveFont(32f));
				int amountX;
				int amountY;
				
				String s = "" + entity.inventory.get(i).amount;
				amountX = getXforAlignToRightText(s, slotX + 44);
				amountY = slotY + gp.tileSize;
				
				// SHADOW
				g2.setColor(new Color(60,60,60));
				g2.drawString(s, amountX, amountY);
				// NUMBER
				g2.setColor(Color.white);
				g2.drawString(s, amountX-3, amountY-3);

			}
			
			slotX += slotSize;
			
			if(i == 4 || i == 9 || i == 14) {
				slotX = slotXstart;
				slotY += slotSize;
			}
		}
		
		
		// CURSOR
		if(cursor == true) {
			int cursorX = slotXstart + (slotSize * slotCol);
			int cursorY = slotYstart + (slotSize * slotRow);
			int cursorWidth = gp.tileSize;
			int cursorHeight = gp.tileSize;
	
			// DRAW CURSOR
			g2.setColor(Color.white);
			g2.setStroke(new BasicStroke(3));
			g2.drawRoundRect(cursorX, cursorY, cursorWidth, cursorHeight, 10, 10);
			
			// DESCRIPTION FRAME
			int dFrameX = frameX;
			int dFrameY = frameY + frameHeight;
			int dFrameWidth = frameWidth;
			int dFrameHeight = gp.tileSize*3;
			
			// DRAW DESCRIPTION TEXT
			int textX = dFrameX + 20; //padding
			int textY = dFrameY + gp.tileSize;
			g2.setFont(g2.getFont().deriveFont(28F));
			
			int itemIndex = getItemIndexOnSlot(slotCol, slotRow);
			
			if(itemIndex < entity.inventory.size()) {
				drawSubWindow(dFrameX, dFrameY, dFrameWidth, dFrameHeight);
				for(String line: entity.inventory.get(itemIndex).description.split("\n")) {
					
					g2.drawString(line, textX, textY);
					textY += 32;
				}
				
				// DURABILITY
				g2.drawString("Durability: " + entity.inventory.get(itemIndex).durability, textX, textY+100);
				
				
			}	
		}

		
		
	}	
	public void drawGameOverScreen(){
		
		g2.setColor(new Color (0,0,0,150));
		g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);;
	
		int x;
		int y;
		String text;
		g2.setFont(g2.getFont().deriveFont(Font.BOLD, 110f));
	
		text = "Game Over";
		
		// SHADOW
		g2.setColor(Color.black);
		x = getXforCenteredText(text);
		y = gp.tileSize*4;
		g2.drawString(text, x, y);
		
		// MAIN
		g2.setColor(Color.white);
		g2.drawString(text, x-4, y-4);

		// RETRY
		g2.setFont(g2.getFont().deriveFont(50f));
		text = "Retry";
		x = getXforCenteredText(text);
		y += gp.tileSize*4;
		g2.drawString(text, x, y);
		if(commandNum == 0) {
			g2.drawString(">", x-40, y);
		}
		
		// BACK TO THE TITLE SCREEN
		text = "Quit";
		x = getXforCenteredText(text);
		y += 55; // BASED ON FONT SIZE OF RETRY
		g2.drawString(text, x, y);
		if(commandNum == 1) {
			g2.drawString(">", x-40, y);
		}
	}
	
	
	public void drawOptionsScreen() {
		
		g2.setColor(Color.white);
		g2.setFont(g2.getFont().deriveFont(25F));

		// SUB WINDOW
		int frameX = gp.tileSize*4;
		int frameY = gp.tileSize;
		int frameWidth = gp.tileSize*8;
		int frameHeight = gp.tileSize*10;
		drawSubWindow(frameX, frameY, frameWidth, frameHeight);
		
		switch(subState) {
		case 0: options_top(frameX, frameY); break;
		case 1: options_control(frameX, frameY); break;
		case 2: options_endGameConfirmation(frameX, frameY); break;

		}

	}
	
	public void triggerSavingMessage() {
	    showSaving = true;
	    saveLoadMessageTime = System.currentTimeMillis();
	}
	public void triggerLoadingMessage() {
	    showLoading = true;
	    saveLoadMessageTime = System.currentTimeMillis();
	}
	
	public void options_top(int frameX, int frameY) {
		
		int textX;
		int textY;
		
		// TITLE
		String text = "Options";
		textX = getXforCenteredText(text);
		textY = frameY + gp.tileSize;
		g2.drawString(text, textX, textY);
		
		// SAVE GAME (0)
		textX = frameX + gp.tileSize;
		
		textY += gp.tileSize;
		g2.drawString("Save Game", textX, textY);
		if (showSaving) {
		    g2.setColor(Color.YELLOW);
		    g2.drawString("Saving...", textX + 165, textY); // adjust 250 for spacing
		    if (System.currentTimeMillis() - saveLoadMessageTime > SAVELOAD_MESSAGE_DURATION) {
		        showSaving = false;
		    }
		    g2.setColor(Color.white);
		}
		if(commandNum == 0) {
			g2.drawString(">", textX-25, textY);
		}
		
		// LOAD GAME (1)
		textY += gp.tileSize;
		g2.drawString("Load Game", textX, textY);
		if (showLoading) {
		    g2.setColor(Color.YELLOW);
		    g2.drawString("Loading...", textX + 165, textY); // adjust as needed
		    if (System.currentTimeMillis() - saveLoadMessageTime > SAVELOAD_MESSAGE_DURATION) {
		        showLoading = false;
		    }
		    g2.setColor(Color.white);
		}
		if(commandNum == 1) {
			g2.drawString(">", textX-25, textY);
		}

		// MUSIC (2)
		textY += gp.tileSize;
		g2.drawString("Music", textX, textY);
		if(commandNum == 2) {
			g2.drawString(">", textX-25, textY);
		}
		
		// SE (3)
		textY += gp.tileSize;
		g2.drawString("Sound Effects", textX, textY);
		if(commandNum == 3) {
			g2.drawString(">", textX-25, textY);
		}
		
		// CONTROL(4)
		textY += gp.tileSize;
		g2.drawString("Controls", textX, textY);
		if(commandNum == 4) {
			g2.drawString(">", textX-25, textY);
			if(gp.keyH.enterPressed == true) {
				subState = 1;
				commandNum = 0;
				gp.keyH.enterPressed = false;
			}	
		}
		
		// EXIT GAME (5)
		textY += gp.tileSize;
		g2.drawString("Exit Game", textX, textY);
		if(commandNum == 5) {
			g2.drawString(">", textX-25, textY);
			if(gp.keyH.enterPressed == true) {
				subState = 2;
				commandNum = 0;
				gp.keyH.enterPressed = false;
			}
		}
		
		// BACK (6)
		textY += gp.tileSize;
		g2.drawString("Back", textX, textY);
		if(commandNum == 6) {
			g2.drawString(">", textX-25, textY);
		    if(gp.keyH.enterPressed == true) {
		        gp.gameState = gp.playState;  // Return to play state
		        commandNum = 0;  // Reset selection
		        gp.keyH.enterPressed = false;
		    }
		}
		
		textX = frameX + gp.tileSize*4 + 24;
		textY = frameY + gp.tileSize*2 + 24; //48 full tile
	
		
		
		// MUSIC VOLUME
		textY += gp.tileSize;
		g2.drawRect(textX, textY, 130, 24); // 130/5 = 26
		int volumeWidth = 26 * gp.music.volumeScale;
		g2.fillRect(textX, textY, volumeWidth, 24);

		// SE VOLUME
		textY += gp.tileSize;
		g2.drawRect(textX, textY, 130, 24); // 130/5 = 26
		volumeWidth = 26 * gp.se.volumeScale;
		g2.fillRect(textX, textY, volumeWidth, 24);
		
		gp.config.saveConfig();

	}
	
	public void options_control(int frameX, int frameY) {
		
		int textX;
		int textY;
		
		// TITLE
		String text = "Controls";
		textX = getXforCenteredText(text);
		textY = frameY + gp.tileSize;		
		g2.drawString(text, textX, textY);
		
		textX = frameX + gp.tileSize;
		textY += gp.tileSize;	
		g2.drawString("Movements", textX, textY); textY += gp.tileSize;
		g2.drawString("Confirm/Attack", textX, textY); textY += gp.tileSize;
		g2.drawString("Character Screen", textX, textY); textY += gp.tileSize;
		g2.drawString("Whole Map", textX, textY); textY += gp.tileSize;
		g2.drawString("Mini-Map", textX, textY); textY += gp.tileSize;
		g2.drawString("Pause", textX, textY); textY += gp.tileSize;
		g2.drawString("Options", textX, textY); textY += gp.tileSize;

		textX = frameX + gp.tileSize*6;
		textY = frameY + gp.tileSize*2;	
		g2.drawString("WASD", textX, textY); textY += gp.tileSize;
		g2.drawString("ENTER", textX, textY); textY += gp.tileSize;
		g2.drawString("C", textX, textY); textY += gp.tileSize; //inventory and status
		g2.drawString("M", textX, textY); textY += gp.tileSize;
		g2.drawString("X", textX, textY); textY += gp.tileSize;
		g2.drawString("P", textX, textY); textY += gp.tileSize;
		g2.drawString("ESC", textX, textY); textY += gp.tileSize;

		// BACK
		textX = frameX + gp.tileSize;
		textY = frameY + gp.tileSize*9;	
		g2.drawString("Back", textX, textY); // INSIDE CONTROLS
		if(commandNum == 0) {
			g2.drawString(">", textX-25, textY);
			if(gp.keyH.enterPressed == true) {
				subState = 0; // BACK TO OPTIONS_TOP
				commandNum = 4; // BACK TO CONTROLS
				gp.keyH.enterPressed = false;
			}
		}

	}
		
	public void options_endGameConfirmation(int frameX, int frameY) {
		
		int textX = frameX + gp.tileSize;
		int textY = frameY + gp.tileSize*3;
		
		currentDialogue = "Quit the game and \nreturn to the title screen?";

		for(String line: currentDialogue.split("\n")) {
			g2.drawString(line, textX, textY);
			textY += 40;
		}
		
		// YES
		String text = "Yes";
		textX = getXforCenteredText(text);
		textY += gp.tileSize*3;
		g2.drawString(text, textX, textY);
		if(commandNum == 0) {
			g2.drawString(">", textX-25, textY);
			if(gp.keyH.enterPressed == true) {
				subState = 0;
	            gp.gameState = gp.titleState;
	            gp.resetGame(true);
				gp.stopMusic();
				gp.keyH.enterPressed = false;
			}
		}
		
		// NO
		text = "No";
		textX = getXforCenteredText(text);
		textY += gp.tileSize;
		g2.drawString(text, textX, textY);
		if(commandNum == 1) {
			g2.drawString(">", textX-25, textY);
			if(gp.keyH.enterPressed == true) {
				subState = 0;
				commandNum = 5;
				gp.keyH.enterPressed = false;
			}
		}
	}
		
	public void drawTransition(){
		
		counter++;
		g2.setColor(new Color(0,0,0,counter*4));
		g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
	
		if(counter == 60) {
			counter = 0;
			gp.gameState = gp.playState;
			gp.currentMap = gp.eHandler.tempMap;
			gp.player.worldX = gp.tileSize*gp.eHandler.tempCol;
			gp.player.worldY = gp.tileSize*gp.eHandler.tempRow;
			gp.eHandler.previousEventX = gp.player.worldX;
			gp.eHandler.previousEventY = gp.player.worldY;
		}
	}
	
	public void drawTradeScreen() {
	    switch (subState) {
	        case 0:
	        	trade_select(); // Buy, Sell, Leave
	            break;
	        case 1:
	        	trade_buy(); // Buying screen
	            break;
	        case 2:
	        	trade_sell(); // Selling screen
	            break;
	    }
	}
	
	public void trade_select(){
		
		npc.dialogueSet = 0;
		drawDialogueScreen();	
		
		// DRAW WINDOW
		int x = gp.tileSize * 11;
		int y = gp.tileSize * 5;
		int width = gp.tileSize * 3;
		int height = (int) (gp.tileSize* 3.7);
		drawSubWindow(x, y, width, height);
		
		// DRAW TEXTS
		x += gp.tileSize;
		y += gp.tileSize;
		
		g2.drawString("Buy", x, y);
		if(commandNum == 0) {
			g2.drawString(">", x-24, y);
		}
		y += gp.tileSize;
		
		g2.drawString("Sell", x, y);
		if(commandNum == 1) {
			g2.drawString(">", x-24, y);
		}
		y += gp.tileSize;
		
		g2.drawString("Leave", x, y);
		if(commandNum == 2) {
			g2.drawString(">", x-24, y);
		}
		
		gp.keyH.enterPressed = false;
	}

	
	public void trade_buy(){
		
		// DRAW PLAYER INVENTORY
		drawInventory(gp.player, false);
			
		// DRAW NPC INVENTORY
		drawInventory(npc, true);
			
		// DRAW HINT WINDOW
		int x = gp.tileSize;
		int y = gp.tileSize*9;
		int width = gp.tileSize*6;
		int height = gp.tileSize*2;
		drawSubWindow(x, y, width, height);
		g2.drawString("[ESC]  Back", x + 24, y + 60); // 48 = 1 tile
			
		// DRAW PLAYER COIN WINDOW
		x = gp.tileSize*9;
		y = gp.tileSize*9;
		width = gp.tileSize*6;
		height = gp.tileSize*2;
		drawSubWindow(x, y, width, height);		
		g2.drawString("Coins: " + gp.player.coin, x + 24, y + 60); // 48 = 1 tile
	
		// DRAW PRICE WINDOW
		int itemIndex = getItemIndexOnSlot(npcSlotCol, npcSlotRow);
		if(itemIndex < npc.inventory.size()) {	// if blank slot, no price
				
			x = (int)(gp.tileSize*4.5);
			y = (int)(gp.tileSize*5.5);
			width = (int)(gp.tileSize*2.5);
			height = gp.tileSize;
			drawSubWindow(x, y, width, height);		
			g2.drawImage(coin, x+10, y+9, 30, 30, null);
			
			int price = npc.inventory.get(itemIndex).price;
			String text = "" + price;
			x = getXforAlignToRightText(text, gp.tileSize*7-12);
			g2.drawString(text, x, y+34);	
		}	
	}
	
	public void trade_sell(){

		// DRAW PLAYER INVENTORY
		drawInventory(gp.player, true);
			
		int x;
		int y;
		int width;
		int height;
		
		// DRAW HINT WINDOW
		x = gp.tileSize;
		y = gp.tileSize*9;
		width = gp.tileSize*6;
		height = gp.tileSize*2;
		drawSubWindow(x, y, width, height);
		g2.drawString("[ESC]  Back", x + 24, y + 60); // 48 = 1 tile
			
		// DRAW PLAYER COIN WINDOW
		x = gp.tileSize*9;
		y = gp.tileSize*9;
		width = gp.tileSize*6;
		height = gp.tileSize*2;
		drawSubWindow(x, y, width, height);		
		g2.drawString("Coins: " + gp.player.coin, x + 24, y + 60); // 48 = 1 tile
	
		// DRAW PRICE WINDOW
		int itemIndex = getItemIndexOnSlot(playerSlotCol, playerSlotRow);
		if(itemIndex < gp.player.inventory.size()) {	// if blank slot, no price
				
			x = (int)(gp.tileSize*12.5);
			y = (int)(gp.tileSize*5.5);
			width = (int)(gp.tileSize*2.5);
			height = gp.tileSize;
			drawSubWindow(x, y, width, height);		
			g2.drawImage(coin, x+10, y+9, 30, 30, null);
		
			int price = gp.player.inventory.get(itemIndex).price;
			String text = "" + (price-(int)(price*0.25));
			x = getXforAlignToRightText(text, gp.tileSize*15-12);
			g2.drawString(text, x, y+34);	// need different textX
		}
	}
	
	public int getItemIndexOnSlot(int slotCol, int slotRow) {
		int itemIndex = slotCol + (slotRow*5); // 4col+(2rowx5) = itemIndex is 14 (0-19)
		return itemIndex;
	}
		
	public void drawSubWindow(int x, int y, int width, int height) {
		
		Color c = new Color(0, 0, 0, 210);
		g2.setColor(c);
		g2.fillRoundRect(x, y, width, height, 35, 35);
		
		c = new Color(255, 255, 255);
		g2.setColor(c);
		g2.setStroke(new BasicStroke(5)); //defines the width of outlines of graphics rendered with a Graphics2D
		g2.drawRoundRect(x+5, y+5, width-10, height-10, 25, 25);
		
	}	
	public int getXforCenteredText(String text) {
		
		int length = (int)g2.getFontMetrics().getStringBounds(text, g2).getWidth();
		int x = (gp.screenWidth - length)/2;
		return x;
	}
	public int getXforAlignToRightText(String text, int tailX) {
		
		int length = (int)g2.getFontMetrics().getStringBounds(text, g2).getWidth();
		int x = tailX - length;
		return x;
	}
	
}//public class UI