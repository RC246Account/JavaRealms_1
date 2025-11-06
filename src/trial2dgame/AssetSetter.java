package trial2dgame;

import entity.Entity;
import entity.NPC_Merchant;
import entity.NPC_OldMan;
import monster.MON_GreenSlime;
import monster.MON_LHound;
import object.OBJ_Chest;
import object.OBJ_Coin;
import object.OBJ_Door;
import object.OBJ_Door_Purple;
import object.OBJ_Key;
import object.OBJ_Key_Purple;
import object.OBJ_Lantern;
import object.OBJ_Potion_Red;
import object.OBJ_Scroll_Violet;
import object.OBJ_Shield_Blue;

import java.util.Random;
import java.awt.Point;
import java.util.HashSet;

public class AssetSetter {

    GamePanel gp;

    public AssetSetter(GamePanel gp) {
        this.gp = gp;
    }

    // Helper functions to add objects, NPC, monsters
    private void addObject(int mapNum, int index, Entity obj, int tileX, int tileY) {
        gp.obj[mapNum][index] = obj;
        gp.obj[mapNum][index].worldX = tileX * gp.tileSize;
        gp.obj[mapNum][index].worldY = tileY * gp.tileSize;
    }

    private void addNPC(int mapNum, int index, Entity npc, int tileX, int tileY) {
        gp.npc[mapNum][index] = npc;
        gp.npc[mapNum][index].worldX = tileX * gp.tileSize;
        gp.npc[mapNum][index].worldY = tileY * gp.tileSize;
    }

    private void addMonster(int mapNum, int index, Entity monster, int tileX, int tileY) {
        gp.monster[mapNum][index] = monster;
        gp.monster[mapNum][index].worldX = tileX * gp.tileSize;
        gp.monster[mapNum][index].worldY = tileY * gp.tileSize;
    }

    public void setObject() {
        int mapNum = 0, i = 0;
        addObject(mapNum, i++, new OBJ_Key(gp), 17, 10);
        addObject(mapNum, i++, new OBJ_Door(gp), 4, 33);
        addObject(mapNum, i++, new OBJ_Shield_Blue(gp), 33, 12);
        addObject(mapNum, i++, new OBJ_Potion_Red(gp), 28, 17);

        // MAP 1
        mapNum = 1;
        i = 0;
        OBJ_Chest chest1 = new OBJ_Chest(gp);
        chest1.setLoot(new OBJ_Key(gp));
        addObject(mapNum, i++, chest1, 33, 28);

        OBJ_Chest chest2 = new OBJ_Chest(gp);
        chest2.setLoot(new OBJ_Potion_Red(gp));
        addObject(mapNum, i++, chest2, 32, 28);

        addObject(mapNum, i++, new OBJ_Lantern(gp), 30, 35);
        addObject(mapNum, i++, new OBJ_Potion_Red(gp), 16, 28);
        addObject(mapNum, i++, new OBJ_Coin(gp), 18, 30);
        addObject(mapNum, i++, new OBJ_Coin(gp), 19, 30);
        addObject(mapNum, i++, new OBJ_Coin(gp), 20, 30);
        addObject(mapNum, i++, new OBJ_Coin(gp), 21, 30);
        addObject(mapNum, i++, new OBJ_Coin(gp), 22, 30);
        addObject(mapNum, i++, new OBJ_Coin(gp), 23, 30);

        // MAP 2
        mapNum = 2;
        i = 0;
        OBJ_Chest chest3 = new OBJ_Chest(gp);
        chest3.setLoot(new OBJ_Lantern(gp));
        addObject(mapNum, i++, chest3, 27, 16);
        OBJ_Chest chest4 = new OBJ_Chest(gp);
        chest4.setLoot(new OBJ_Scroll_Violet(gp));
        addObject(mapNum, i++, chest4, 36, 17);

        // MAP 3
        mapNum = 3;
        i = 0;
        addObject(mapNum, i++, new OBJ_Door_Purple(gp), 25, 22);
        addObject(mapNum, i++, new OBJ_Key_Purple(gp), 20, 24);
    }

    public void setNPC() {
        int mapNum = 0, i = 0;
        addNPC(mapNum, i++, new NPC_OldMan(gp), 25, 14);

        // MAP 1
        mapNum = 1;
        i = 0;
        addNPC(mapNum, i++, new NPC_Merchant(gp), 25, 31);
    }

    // ---- MONSTER WAVE/ANSWER INDEX FEATURE ----

    private final int[][] spawnWaves = {
        {5, 5, 5}, // Map 1
        {7, 7, 6}, // Map 2
        {9, 8, 8}, // Map 3
        {7, 7, 7, 9}, // Map 4
        {1} // Map 5 (boss)
    };

    private int currentMap = 0;
    private int waveCounter = 0;

    private Random rand = new Random();
    private HashSet<Point> usedCoordinates = new HashSet<>();

    private Point getRandomTile(GamePanel gp) {
        int x, y;
        Point p;
        do {
            x = rand.nextInt(gp.maxWorldCol);
            y = rand.nextInt(gp.maxWorldRow);
            p = new Point(x, y);
        } while (usedCoordinates.contains(p));
        usedCoordinates.add(p);
        return p;
    }

    // Call at map/battle start, and after each wave finished
    public void spawnNextWave(GamePanel gp) {
        usedCoordinates.clear();
        if (waveCounter < spawnWaves[gp.currentMap].length) {
            int num = spawnWaves[gp.currentMap][waveCounter];
            for (int i = 0; i < num; i++) {
                Point pos = getRandomTile(gp);
                Entity monster;
                // Choose monster per map or type if you like:
                if (gp.currentMap == 0) {
                    monster = new MON_GreenSlime(gp);
                } else if (gp.currentMap == 1) {
                    monster = new MON_LHound(gp);
                } else {
                    monster = new MON_GreenSlime(gp); // fallback
                }
                monster.worldX = pos.x * gp.tileSize;
                monster.worldY = pos.y * gp.tileSize;
                monster.answerIndex = i; // assign number tied to answer!
                gp.monster[gp.currentMap][i] = monster;
            }
            waveCounter++;
        }
    }

    // Optionally reset for new map
    public void resetWaves(int mapNum) {
        currentMap = mapNum;
        waveCounter = 0;
    }

    // Legacy setMonster still works for static/manual test placement:
    public void setMonster() {
        int mapNum = 0, i = 0;
        addMonster(mapNum, i++, new MON_GreenSlime(gp), 40, 5);
        addMonster(mapNum, i++, new MON_GreenSlime(gp), 36, 9);
        addMonster(mapNum, i++, new MON_GreenSlime(gp), 32, 4);
        addMonster(mapNum, i++, new MON_GreenSlime(gp), 39, 2);
        addMonster(mapNum, i++, new MON_GreenSlime(gp), 38, 10);
        addMonster(mapNum, i++, new MON_GreenSlime(gp), 30, 1);

        // MAP 2
        mapNum = 2;
        i = 0;
        addMonster(mapNum, i++, new MON_LHound(gp), 9, 36);
    }
}
