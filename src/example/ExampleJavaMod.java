package example;

import arc.util.Log;
import mindustry.mod.Mod;

public class ExampleJavaMod extends Mod{

    public ExampleJavaMod(){
        Log.info("Goblin Storage v0.3.0 loaded!");
    }

    @Override
    public void loadContent(){
        Log.info("Goblin Storage content loading...");
    }
}
