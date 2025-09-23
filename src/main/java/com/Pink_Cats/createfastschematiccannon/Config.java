package com.Pink_Cats.createfastschematiccannon;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = Createfastschematiccannon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue ENABLE_CFC = BUILDER
            .comment("Why does this configuration file have two languages: This is what my Chinese friend wanted, so I did it this way, haha.")
            .comment("_________________________________________________________________________________________ ")
            .comment("请注意： 在某些服务器核心内(Mohist/Arclight)，这个配置文件的修改必须要重启整个服务器才能生效！")
            .comment("Please note: In some server cores(Mohist/Arclight), modifications to this configuration file require a full server restart to take effect!")
            .comment("")
            .comment("__________________________________________________________________________________________")
            .comment("")
            .comment("Enable this change or not!")
            .comment("是否启用蓝图炮加速!")
            .define("EnableCannonSpeedUp", true);



    public  static final ForgeConfigSpec.IntValue SCHEMATIC_SPEED_UP_PER_TICK = BUILDER
            .comment("__________________________________________________________________________________________")
            .comment("")
            .comment("This variable represents the number of prints the blueprint cannon performs per tick, which is multiplicative with the print delay within the blueprint cannon.")
            .comment("[Blueprint Cannon Print Delay] is configured in Create and can be found at: Gameplay Settings/Schematics/Schematicannon/Schematicannon Delay.")
            .comment("If set to 20, and the [Blueprint Cannon Print Delay] is set to 1, then the print speed of the blueprint cannon would be 20 * 20 / 1 = 400 times.")
            .comment("The calculation formula is: Print Speed = Number of Prints per Tick (this configuration value) x Game Ticks (default is 20) / [Blueprint Cannon Print Delay] (default is 10).")
            .comment("这个变量代表每tick蓝图炮进行的打印次数，与蓝图炮内的打印延时是相乘关系")
            .comment("[蓝图炮打印延时]为机械动力本体配置，位于：Gameplay Settings/Schematics/Schematicannon/Schematicannon Delay ")
            .comment("如果设置为 20 ，[蓝图炮打印延时]设置为1 那么蓝图炮的打印速度为 20 * 20/1 = 400倍")
            .comment("计算公式为 ：    打印速度 = 每tick蓝图炮打印的次数（本配置值） x 游戏刻（默认为20） /  [蓝图炮打印延时]（默认为10）      ")

            .defineInRange("SpeedupPerTick", 20, 1,400 );

    public static final ForgeConfigSpec.IntValue LAZY_TICK = BUILDER
            .comment("__________________________________________________________________________________________")
            .comment("")
            .comment("This is an experimental parameter used to reduce the game ticks of the blueprint cannon.")
            .comment("It specifies how many ticks should pass before executing the blueprint cannon code.")
            .comment("The default value in Create is 1, but you can increase it to reduce lag. However, be aware that this will make the blueprint cannon's response slower.")
            .comment("A suggested value is 5, which is four times slower than the default in Create, but the difference may not be noticeable. If you feel the blueprint cannon's response is too slow, consider lowering this value.")
            .comment("这个是实验性参数，用于减少蓝图炮的游戏刻")
            .comment("即每多少刻，执行一次蓝图炮代码")
            .comment("机械动力原版默认为 1 但是您可以将其改高来削减卡顿，但是注意，这会导致蓝图炮的反应变慢")
            .comment("建议的值 5 与机械动力原版相比慢了4倍，但是感觉不出来变化，如果觉得蓝图炮反应变慢，适当降低这个值")
            .defineInRange("lazyTick", 5, 1,200 );


    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKS_STRING =
            BUILDER.comment("--------------------------------------------------------------------------")
                    .comment("A list of blocks can't be clear or broke by cannon.")
                    .comment("列表内的方块不会被蓝图炮摧毁，这可以阻止蓝图炮无尽锅炉bug")
                    .defineListAllowEmpty("blocks_unbreak", List.of(
                            "create:blaze_burner"
                    ), Config::validateItemName);

    private static final ForgeConfigSpec.BooleanValue DEBUG = BUILDER
            .comment("Enable forbid message or not")
            .comment("当蓝图炮尝试破坏被禁止的方块时是否在控制台通知")
            .define("debug", true);





    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean enable_CFC;
    public static int lazyTick;
    public static int SchematicSpeedupPerTick;
    public static Set<String> blocks_unbreak; // 定义为 Set<String>
    public static boolean enable_debug;


    private static boolean validateItemName(final Object obj) {
        return obj instanceof final String itemName && ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(itemName));
    }


    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        enable_CFC = ENABLE_CFC.get();
        SchematicSpeedupPerTick = SCHEMATIC_SPEED_UP_PER_TICK.get();
        lazyTick = LAZY_TICK.get();
        blocks_unbreak = new HashSet<>(BLACKS_STRING.get());
        enable_debug = DEBUG.get();

    }
}
