package com.app.util;

import com.app.model.LogEntry;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GanttChartRenderer {
    private static final double LEFT_PADDING = 70; // Left margin for labels and tick origin
    private static final double TOP_PADDING = 30; // Top margin before first timeline row
    private static final double ROW_HEIGHT = 38; // Vertical spacing between log rows
    private static final double BAR_HEIGHT = 28; // Height of each rendered execution bar
    private static final double UNIT_WIDTH = 28; // Horizontal pixels per one time unit
    private static final List<Color> PALETTE = List.of(
        Color.web("#3B82F6"), Color.web("#EF4444"), Color.web("#10B981"), Color.web("#F59E0B"),
        Color.web("#8B5CF6"), Color.web("#EC4899"), Color.web("#14B8A6"), Color.web("#F97316"),
        Color.web("#6366F1"), Color.web("#22C55E")
    );

    public ScrollPane render(List<LogEntry> entries, String title) {
        int maxTime = entries.stream().mapToInt(LogEntry::end).max().orElse(0);
        // Keep a wide baseline so short schedules still have readable horizontal spacing
        double width = Math.max(1100, LEFT_PADDING + 50 + (maxTime * UNIT_WIDTH));
        double height = TOP_PADDING + 90 + (entries.size() * ROW_HEIGHT);
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.web("#0F172A"));
        gc.fillRect(0, 0, width, height);
        gc.setFont(Font.font("Consolas", 14));
        gc.setFill(Color.web("#E2E8F0"));
        gc.fillText(title, LEFT_PADDING, 18);

        Map<Integer, Color> colorByPid = new HashMap<>();
        // For drawing vertical timeline grid
        for (int i = 0; i <= maxTime; i++) {
            double x = LEFT_PADDING + (i * UNIT_WIDTH);
            gc.setStroke(i % 2 == 0 ? Color.web("#334155") : Color.web("#1E293B"));
            gc.strokeLine(x, TOP_PADDING - 8, x, height - 20);
            gc.setFill(Color.web("#94A3B8"));
            gc.fillText(String.valueOf(i), x - 4, height - 4);
        }

        // For drawing one colored bar per execution log entry
        for (int i = 0; i < entries.size(); i++) {
            LogEntry entry = entries.get(i);
            colorByPid.putIfAbsent(entry.pid(), PALETTE.get(colorByPid.size() % PALETTE.size()));
            double y = TOP_PADDING + (i * ROW_HEIGHT);
            double x = LEFT_PADDING + (entry.start() * UNIT_WIDTH);
            double w = Math.max(UNIT_WIDTH * (entry.end() - entry.start()), 1);

            gc.setFill(colorByPid.get(entry.pid()));
            gc.fillRoundRect(x, y, w, BAR_HEIGHT, 10, 10);
            gc.setStroke(Color.web("#0B1020"));
            gc.strokeRoundRect(x, y, w, BAR_HEIGHT, 10, 10);

            gc.setFill(Color.WHITE);
            gc.setTextAlign(TextAlignment.CENTER);
            String label = (entry.isFinished() ? "FIN " : "") + "P" + entry.pid();
            gc.fillText(label, x + (w / 2), y + 18);
            gc.setTextAlign(TextAlignment.LEFT);
        }

        StackPane holder = new StackPane(canvas);
        holder.setPadding(new Insets(12));
        holder.setStyle("-fx-background-color: #0F172A;");

        VBox content = new VBox(holder);
        content.setStyle("-fx-background-color: #0F172A;");
        content.setFillWidth(true);
        content.setMinWidth(0);
        content.setPrefWidth(width + 24);
        content.setMaxWidth(Region.USE_PREF_SIZE);

        // Preserve wide preferred content and use horizontal scrolling instead of squeezing
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(false);
        scrollPane.setMinWidth(0);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: #0F172A; -fx-background-color: #0F172A;");
        return scrollPane;
    }
}
