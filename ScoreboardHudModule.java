package com.example.visuals.modules;

import com.example.visuals.gui.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/** Draws the server's sidebar scoreboard in the Cold style; you can drag it (see InGameHudMixin). */
public class ScoreboardHudModule extends Module {
    public final Setting numbers = add(Setting.bool("Show Numbers", true));

    private ScoreboardObjective objective;
    private long seen;

    public ScoreboardHudModule() {
        super("Scoreboard", "Restyled server scoreboard (draggable)");
        makeMovable(10000, 110);
    }

    /** Called by the mixin every frame the vanilla scoreboard would have been drawn. */
    public void capture(ScoreboardObjective o) {
        objective = o;
        seen = System.currentTimeMillis();
    }

    @Override
    public void onHud(DrawContext ctx, MinecraftClient mc) {
        if (objective == null || System.currentTimeMillis() - seen > 250) return;
        Scoreboard sb = objective.getScoreboard();

        List<ScoreboardEntry> list = new ArrayList<>(sb.getScoreboardEntries(objective));
        list.removeIf(ScoreboardEntry::hidden);
        list.sort((a, b) -> a.value() != b.value()
                ? Integer.compare(b.value(), a.value())
                : a.owner().compareToIgnoreCase(b.owner()));
        if (list.size() > 15) list = new ArrayList<>(list.subList(0, 15));

        TextRenderer tr = mc.textRenderer;
        Text title = objective.getDisplayName();
        List<Text> names = new ArrayList<>();
        for (ScoreboardEntry e : list) names.add(Team.decorateName(sb.getScoreHolderTeam(e.owner()), e.name()));

        int w = tr.getWidth(title) + 16;
        for (int i = 0; i < list.size(); i++) {
            int rowW = tr.getWidth(names.get(i)) + 12;
            if (numbers.asBool()) rowW += tr.getWidth(String.valueOf(list.get(i).value())) + 10;
            w = Math.max(w, rowW);
        }
        int rowH = 10;
        int h = (list.size() + 1) * rowH + 12;
        int px = clampX(ctx, w), py = clampY(ctx, h);
        setBox(px, py, w, h);

        RenderUtil.roundedRect(ctx, px, py, w, h, 4, 0xB0101018);
        ctx.drawTextWithShadow(tr, title, px + (w - tr.getWidth(title)) / 2, py + 4, 0xFFFFFFFF);
        ctx.fill(px + 6, py + rowH + 4, px + w - 6, py + rowH + 5, 0x55B06CFF);

        int ty = py + rowH + 8;
        for (int i = 0; i < list.size(); i++) {
            ctx.drawTextWithShadow(tr, names.get(i), px + 6, ty, 0xFFFFFFFF);
            if (numbers.asBool()) {
                String v = String.valueOf(list.get(i).value());
                ctx.drawTextWithShadow(tr, v, px + w - 6 - tr.getWidth(v), ty, 0xFFFF6B6B);
            }
            ty += rowH;
        }
    }
}
