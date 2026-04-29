package com.nexusabsolu.mod.commands;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Commande de test sound pour debugger pourquoi la musique du Manifold ne
 * joue pas. Essaie 3 methodes en sequence avec 3 secondes entre chaque, et
 * log abondamment cote client.
 *
 * Usage :  /nexus_test_sound
 *
 * Methodes testees :
 *   1. ITickableSound (la methode actuelle de la cartouche)
 *   2. world.playSound(player, ...) -- methode classique 1.12.2
 *   3. SoundHandler.playSound + PositionedSoundRecord.getMasterRecord
 *
 * Si AUCUNE des 3 ne marche : probleme de chargement de l'OGG ou de
 * registration du SoundEvent.
 * Si seule la 2 ou 3 marche : probleme avec ITickableSound.
 *
 * v1.0.341 (debug session musique).
 */
public class CommandNexusTestSound extends CommandBase {

    public CommandNexusTestSound() {}

    @Override public String getName() { return "nexus_test_sound"; }
    @Override public String getUsage(ICommandSender sender) { return "/nexus_test_sound"; }
    @Override public int getRequiredPermissionLevel() { return 0; }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayerMP)) {
            sender.sendMessage(new TextComponentString("Cette commande doit etre executee par un joueur."));
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) sender;
        player.sendMessage(new TextComponentString(
            "[NexusTestSound] Test des 3 methodes de play. Regarde les logs cote client (F3+T pour les voir)."));

        com.nexusabsolu.mod.network.NexusPacketHandler.INSTANCE.sendTo(
            new TestSoundPacket(), player);
    }

    /**
     * Packet S->C qui declenche les 3 tests cote client.
     */
    public static class TestSoundPacket implements IMessage {

        public TestSoundPacket() {}

        @Override public void fromBytes(ByteBuf buf) {}
        @Override public void toBytes(ByteBuf buf) {}

        public static class Handler implements IMessageHandler<TestSoundPacket, IMessage> {
            @Override
            @SideOnly(Side.CLIENT)
            public IMessage onMessage(TestSoundPacket msg, MessageContext ctx) {
                Minecraft.getMinecraft().addScheduledTask(() -> runTests());
                return null;
            }

            @SideOnly(Side.CLIENT)
            private void runTests() {
                Minecraft mc = Minecraft.getMinecraft();
                System.out.println("==================================================");
                System.out.println("[NexusTestSound] DEBUT DES TESTS SOUND");
                System.out.println("==================================================");

                // === Verifier que le sound event existe ===
                net.minecraft.util.SoundEvent ev = com.nexusabsolu.mod.init.ModSounds.MANIFOLD_CENTINELA;
                System.out.println("[NexusTestSound] ModSounds.MANIFOLD_CENTINELA = " + ev);
                if (ev != null) {
                    System.out.println("[NexusTestSound]   registryName = " + ev.getRegistryName());
                }

                // === Verifier les volumes utilisateur ===
                float musicVol = mc.gameSettings.getSoundLevel(net.minecraft.util.SoundCategory.MUSIC);
                float masterVol = mc.gameSettings.getSoundLevel(net.minecraft.util.SoundCategory.MASTER);
                System.out.println("[NexusTestSound] Volume MASTER = " + masterVol + " / MUSIC = " + musicVol);
                if (musicVol < 0.01f) {
                    System.out.println("[NexusTestSound] !!! CATEGORIE MUSIC EST A " + musicVol
                        + " !!! Mets-la a fond dans Options > Sons > Musique !");
                    mc.player.sendMessage(new TextComponentString(
                        "[NexusTestSound] !! Ton volume MUSIC est a " + (int)(musicVol * 100)
                        + "%. Mets-le a 100% dans Options > Sons."));
                }
                if (masterVol < 0.01f) {
                    System.out.println("[NexusTestSound] !!! VOLUME MASTER EST A " + masterVol
                        + " !!! Mets-le a fond aussi !");
                }

                if (ev == null) {
                    System.out.println("[NexusTestSound] SoundEvent null, abandon des tests.");
                    mc.player.sendMessage(new TextComponentString(
                        "[NexusTestSound] !! ModSounds.MANIFOLD_CENTINELA est null. Le sound n'est pas registered."));
                    return;
                }

                // === Test 1 : ITickableSound (methode actuelle) ===
                System.out.println("[NexusTestSound] TEST 1 : ITickableSound...");
                try {
                    com.nexusabsolu.mod.client.ManifoldMusicTickableSound s1 =
                        new com.nexusabsolu.mod.client.ManifoldMusicTickableSound(ev);
                    mc.getSoundHandler().playSound(s1);
                    System.out.println("[NexusTestSound]   Test 1 lance.");
                    mc.player.sendMessage(new TextComponentString(
                        "[NexusTestSound] Test 1/3 (ITickableSound) lance. Tu devrais entendre la musique..."));
                } catch (Throwable t) {
                    System.out.println("[NexusTestSound]   Test 1 ECHEC : " + t.getMessage());
                    t.printStackTrace();
                }

                // === Test 2 (delai 4s) : world.playSound ===
                schedule(80, () -> {
                    System.out.println("[NexusTestSound] TEST 2 : world.playSound (methode classique)...");
                    try {
                        net.minecraft.client.Minecraft mc2 = net.minecraft.client.Minecraft.getMinecraft();
                        if (mc2.world != null && mc2.player != null) {
                            mc2.world.playSound(mc2.player.posX, mc2.player.posY, mc2.player.posZ,
                                ev, net.minecraft.util.SoundCategory.MUSIC,
                                1.0F, 1.0F, false);
                            System.out.println("[NexusTestSound]   Test 2 lance.");
                            mc2.player.sendMessage(new TextComponentString(
                                "[NexusTestSound] Test 2/3 (world.playSound) lance."));
                        } else {
                            System.out.println("[NexusTestSound]   Test 2 SKIPPED : world ou player null.");
                        }
                    } catch (Throwable t) {
                        System.out.println("[NexusTestSound]   Test 2 ECHEC : " + t.getMessage());
                        t.printStackTrace();
                    }
                });

                // === Test 3 (delai 8s) : PositionedSoundRecord.getMasterRecord ===
                schedule(160, () -> {
                    System.out.println("[NexusTestSound] TEST 3 : PositionedSoundRecord.getMasterRecord...");
                    try {
                        net.minecraft.client.Minecraft mc3 = net.minecraft.client.Minecraft.getMinecraft();
                        net.minecraft.client.audio.PositionedSoundRecord record =
                            net.minecraft.client.audio.PositionedSoundRecord.getMasterRecord(ev, 1.0F);
                        mc3.getSoundHandler().playSound(record);
                        System.out.println("[NexusTestSound]   Test 3 lance.");
                        mc3.player.sendMessage(new TextComponentString(
                            "[NexusTestSound] Test 3/3 (PositionedSoundRecord) lance."));
                    } catch (Throwable t) {
                        System.out.println("[NexusTestSound]   Test 3 ECHEC : " + t.getMessage());
                        t.printStackTrace();
                    }
                });

                // === Resume ===
                schedule(240, () -> {
                    System.out.println("==================================================");
                    System.out.println("[NexusTestSound] FIN DES TESTS. Si tu n'as rien entendu :");
                    System.out.println("[NexusTestSound]  1. Verifie volumes MASTER + MUSIC dans Options > Sons");
                    System.out.println("[NexusTestSound]  2. Verifie que centinela.ogg est bien dans le jar");
                    System.out.println("[NexusTestSound]  3. Si OptiFine, essaie sans");
                    System.out.println("==================================================");
                    Minecraft.getMinecraft().player.sendMessage(new TextComponentString(
                        "[NexusTestSound] Tests termines. Si tu n'entends rien, verifie volumes MASTER + MUSIC."));
                });
            }

            @SideOnly(Side.CLIENT)
            private void schedule(int delayTicks, Runnable r) {
                // Delay via repeat scheduledTask -- simple impl
                Minecraft mc = Minecraft.getMinecraft();
                new Thread(() -> {
                    try {
                        Thread.sleep(delayTicks * 50L); // 50ms / tick
                    } catch (InterruptedException ignored) {}
                    mc.addScheduledTask(r);
                }, "NexusTestSound-delay").start();
            }
        }
    }
}
