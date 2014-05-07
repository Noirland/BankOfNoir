package nz.co.noirland.bankofnoir.config;

import nz.co.noirland.bankofnoir.BankOfNoir;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

public abstract class Config {

    // Config setup borrowed from mcMMO - Thanks!

    protected static final BankOfNoir plugin = BankOfNoir.inst();
    protected final String file;
    protected final File configFile;
    protected FileConfiguration config;


    public Config(String path, String file) {
        this.file = file;
        configFile = new File(plugin.getDataFolder(), path + File.separator + file);
        loadFile();

    }

    public Config(String file) {
        this.file = file;
        configFile = new File(plugin.getDataFolder(), file);
        loadFile();
    }

    public Config(File file) {
        this.file = file.getName();
        configFile = file;
        loadFile();
    }

    protected void loadFile() {
        if(!configFile.exists()) {
            createFile();
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    protected void createFile() {
        configFile.getParentFile().mkdirs();

        InputStream iStream = getResource();

        if(iStream == null) {
            plugin.getLogger().severe("File missing from jar: " + file);
            return;
        }

        OutputStream oStream = null;

        try {
            oStream = new FileOutputStream(configFile);

            int read;
            byte[] bytes = new byte[1024];

            while((read = iStream.read(bytes)) != -1) {
                oStream.write(bytes, 0, read);
            }

        }catch(FileNotFoundException e) {
            e.printStackTrace();
        }catch(IOException e) {
            e.printStackTrace();
        }finally{

            if(oStream != null) {
                try {
                    oStream.close();
                }catch(IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                iStream.close();
            }catch(IOException e) {
                e.printStackTrace();
            }

        }

    }

    protected InputStream getResource() {

        return plugin.getResource(file);

    }



}
