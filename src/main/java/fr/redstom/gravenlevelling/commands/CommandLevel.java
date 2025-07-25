package fr.redstom.gravenlevelling.commands;

import fr.redstom.gravenlevelling.jpa.entities.GravenMember;
import fr.redstom.gravenlevelling.jpa.services.GravenMemberService;
import fr.redstom.gravenlevelling.utils.ImageGenerator;
import fr.redstom.gravenlevelling.utils.jda.Command;
import fr.redstom.gravenlevelling.utils.jda.CommandExecutor;
import fr.redstom.gravenlevelling.utils.jda.EmbedUtils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

@Command
@RequiredArgsConstructor
public class CommandLevel implements CommandExecutor {

    private final GravenMemberService memberService;
    private final ImageGenerator imageGenerator;

    @Override
    public SlashCommandData data() {
        return Commands.slash("level", "Permet de voir votre niveau actuel sur le serveur")
                .addOption(
                        OptionType.USER, "user", "Membre dont vous voulez savoir le niveau", false)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
    }

    @SneakyThrows
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member discordMember =
                event.getOption("user", event.getMember(), OptionMapping::getAsMember);

        if (discordMember == null) {
            event.replyEmbeds(EmbedUtils.error("Impossible de trouver le membre spécifié").build())
                    .queue();
            return;
        }

        InteractionHook hook = event.deferReply().complete();

        GravenMember member = memberService.getMemberByDiscordMember(discordMember);

        BufferedImage image = imageGenerator.generateLevelImage(discordMember, member);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", stream);
        stream.flush();

        hook.editOriginalAttachments(FileUpload.fromData(stream.toByteArray(), "image.png"))
                .queue();
    }
}
