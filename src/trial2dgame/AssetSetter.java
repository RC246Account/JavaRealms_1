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

public class AssetSetter {

	GamePanel gp;

//ASSESTSETTER()	
	public AssetSetter(GamePanel gp) {
		this.gp = gp;
	}

//NEW CODE!! ADDOBJECT()
	private void addObject(int mapNum, int index, Entity obj, int tileX, int tileY) {
	    gp.obj[mapNum][index] = obj;
	    gp.obj[mapNum][index].worldX = tileX * gp.tileSize;
	    gp.obj[mapNum][index].worldY = tileY * gp.tileSize;
	}

//NEW CODE!! ADDNPC()	
	private void addNPC(int mapNum, int index, Entity npc, int tileX, int tileY) {
	    gp.npc[mapNum][index] = npc;
	    gp.npc[mapNum][index].worldX = tileX * gp.tileSize;
	    gp.npc[mapNum][index].worldY = tileY * gp.tileSize;
	}

//NEW CODE!! ADDMONSTER()	
	private void addMonster(int mapNum, int index, Entity monster, int tileX, int tileY) {
	    gp.monster[mapNum][index] = monster;
	    gp.monster[mapNum][index].worldX = tileX * gp.tileSize;
	    gp.monster[mapNum][index].worldY = tileY * gp.tileSize;
	}
	
//SETOBJECT()	
	public void setObject() {
		
		// MAP 0
		int mapNum = 0;
		int i = 0;
		
		addObject(mapNum, i++, new OBJ_Key(gp), 17, 10);
		addObject(mapNum, i++, new OBJ_Door(gp), 4, 33);
		addObject(mapNum, i++, new OBJ_Shield_Blue(gp), 33, 12);
		addObject(mapNum, i++, new OBJ_Potion_Red(gp), 28, 17);

		// MAP 1
		mapNum = 1;
		i = 0;
		
		//use this format if has extra setting
		OBJ_Chest chest1 = new OBJ_Chest(gp);	// instantiation
		chest1.setLoot(new OBJ_Key(gp));			// composition + encapsulation
		addObject(mapNum, i++, chest1, 33, 28);	// abstraction + polymorphism

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
		
		//Map 02
		mapNum = 2;
		i = 0;
		
		OBJ_Chest chest3 = new OBJ_Chest(gp);
		chest3.setLoot(new OBJ_Lantern(gp));
		addObject(mapNum, i++, chest3, 27, 16);
		
		OBJ_Chest chest4 = new OBJ_Chest(gp);
		chest4.setLoot(new OBJ_Scroll_Violet(gp));
		addObject(mapNum, i++, chest4, 36, 17);
		
		//Map 03
		mapNum = 3;
		i = 0;
		
		addObject(mapNum, i++, new OBJ_Door_Purple(gp), 25, 22);
		addObject(mapNum, i++, new OBJ_Key_Purple(gp), 20, 24);

	}

//SETNPC()	
	public void setNPC() {
		
		int mapNum = 0;
		int i = 0;
		
		// MAP 0
		addNPC(mapNum, i++, new NPC_OldMan(gp), 25, 14);
		
		// MAP 1
		mapNum = 1;
		i = 0;
		
		addNPC(mapNum, i++, new NPC_Merchant(gp), 25, 31);

	}
	
	public void setMonster() {
		
		int mapNum = 0;
		int i = 0;
		
		// MAP 0
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
