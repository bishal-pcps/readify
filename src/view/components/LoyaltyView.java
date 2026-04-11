package view.components;

import java.sql.SQLException;
import java.util.List;

import controller.AuthController;
import controller.CustomerController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import model.LoyaltyActivity;
import model.User;

/**
 * Loyalty Rewards view — matches Loyalty.tsx design.
 * Shows points, tier, progress bar, reward cards, activity feed.
 */
public class LoyaltyView {
    private Stage stage;
    private CustomerController customerController;
    private Runnable onBackClick;
    private Runnable onLogoutClick;
    private Runnable onOrdersClick;
    private Runnable onCartClick;

    // Tier thresholds
    private static final int[][] TIERS = {
        {0, 1000},    // Bronze: 0-999
        {1000, 2000}, // Silver: 1000-1999
        {2000, 4000}, // Gold:   2000-3999
        {4000, Integer.MAX_VALUE} // Platinum: 4000+
    };
    private static final String[] TIER_NAMES   = {"Bronze", "Silver", "Gold", "Platinum"};
    private static final String[] TIER_COLORS  = {"#CD7F32", "#C0C0C0", "#D4AF37", "#B0C4DE"};
    private static final int[][] REWARDS = {
        {500, 0},   // "$5 Off"
        {750, 0},   // "Free Shipping"
        {1000, 0},  // "$10 Off"
        {1500, 0},  // "Early Access"
        {2500, 0},  // "$25 Off"
        {5000, 1},  // "VIP Status" — coming soon
    };
    private static final String[] REWARD_TITLES = {
        "$5 Off Your Next Order", "Free Shipping", "$10 Off Your Next Order",
        "Exclusive Book Preview", "$25 Off Your Next Order", "VIP Member Status"
    };
    private static final String[] REWARD_DESC = {
        "Save on any purchase", "On orders over $25", "Save on any purchase",
        "Early access to new releases", "Save on any purchase", "Exclusive perks and benefits"
    };

    public LoyaltyView(Stage stage, AuthController authController, CustomerController customerController, Runnable onBackClick, Runnable onLogoutClick) {
        this.stage = stage;
        this.customerController = customerController;
        this.onBackClick = onBackClick;
        this.onLogoutClick = onLogoutClick;
        this.onOrdersClick = null;
        this.onCartClick = null;
    }

    public void setNavCallbacks(Runnable onOrdersClick, Runnable onCartClick) {
        this.onOrdersClick = onOrdersClick;
        this.onCartClick = onCartClick;
    }

    public Scene createLoyaltyScene() {
        User current = AuthController.getCurrentCustomer();
        int userId = (current != null) ? current.getUserId() : 0;
        int userPoints = 0;
        List<LoyaltyActivity> history = null;

        try {
            if (userId > 0) {
                userPoints = customerController.getLoyaltyPoints(userId);
                history = customerController.getLoyaltyHistory(userId);
            }
        } catch (SQLException e) {
            userPoints = 0;
        }

        String currentTier = getTierName(userPoints);
        String nextTier = getNextTierName(userPoints);
        int pointsToNext = getPointsToNextTier(userPoints);
        double progress = getProgress(userPoints);
        String tierColor = getTierColor(userPoints);

        // Root
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #FDF8F0;");

        // Nav
        HBox nav = NavigationBar.create(false, "loyalty",
            onBackClick, onCartClick, onOrdersClick, null,
            null, null, null, null, onLogoutClick, 0);

        // Scrollable content
        VBox content = new VBox(32);
        content.setPadding(new Insets(32, 48, 40, 48));
        content.setStyle("-fx-background-color: #FDF8F0;");

        // ── Header ──
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        StackPane headerIcon = makeIconBox("#D4AF37", "M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z");
        Label headerTitle = new Label("Loyalty Rewards");
        headerTitle.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #5D4A3A;");
        Region hSpacer = new Region(); HBox.setHgrow(hSpacer, Priority.ALWAYS);
        Button backBtn = new Button("Back to Browse");
        backBtn.setStyle(secondaryBtnStyle());
        backBtn.setOnAction(e -> onBackClick.run());
        header.getChildren().addAll(headerIcon, headerTitle, hSpacer, backBtn);

        // ── 3 KPI Cards ──
        HBox kpiRow = new HBox(20);
        VBox pointsCard = makeKPICard("Your Points", String.format("%,d", userPoints),
            "⭐  Earn 10 points per $1 spent", "#D4AF37");
        VBox tierCard = makeTierCard(currentTier, tierColor);
        VBox progressCard = makeProgressCard(nextTier, pointsToNext, progress);
        HBox.setHgrow(pointsCard, Priority.ALWAYS);
        HBox.setHgrow(tierCard, Priority.ALWAYS);
        HBox.setHgrow(progressCard, Priority.ALWAYS);
        kpiRow.getChildren().addAll(pointsCard, tierCard, progressCard);

        // ── Tier Levels ──
        VBox tierSection = new VBox(14);
        Label tierHeader = new Label("Membership Tiers");
        tierHeader.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #5D4A3A;");
        HBox tiersRow = new HBox(16);
        for (int i = 0; i < TIER_NAMES.length; i++) {
            tiersRow.getChildren().add(makeTierBadge(TIER_NAMES[i], TIER_COLORS[i],
                TIERS[i][0], TIER_NAMES[i].equals(currentTier)));
            HBox.setHgrow(tiersRow.getChildren().get(i), Priority.ALWAYS);
        }
        tierSection.getChildren().addAll(tierHeader, tiersRow);

        // ── Rewards + Activity ──
        HBox bottomRow = new HBox(24);

        // Rewards grid
        VBox rewardsSection = new VBox(14);
        HBox.setHgrow(rewardsSection, Priority.ALWAYS);
        Label rewardsHeader = new Label("Available Rewards");
        rewardsHeader.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #5D4A3A;");
        GridPane rewardsGrid = new GridPane();
        rewardsGrid.setHgap(16); rewardsGrid.setVgap(16);
        ColumnConstraints cc1 = new ColumnConstraints(); cc1.setPercentWidth(50);
        ColumnConstraints cc2 = new ColumnConstraints(); cc2.setPercentWidth(50);
        rewardsGrid.getColumnConstraints().addAll(cc1, cc2);

        for (int i = 0; i < REWARDS.length; i++) {
            boolean comingSoon = REWARDS[i][1] == 1;
            boolean canRedeem = !comingSoon && userPoints >= REWARDS[i][0];
            final int ptsToRedeem = REWARDS[i][0];
            final String rewardName = REWARD_TITLES[i];
            final int finalUserId = userId;

            VBox rewardCard = makeRewardCard(rewardName, REWARD_DESC[i],
                ptsToRedeem, canRedeem, comingSoon);

            Button redeemBtn = (Button) rewardCard.getChildren().get(1);
            redeemBtn.setOnAction(e -> {
                try {
                    customerController.redeemPoints(finalUserId, ptsToRedeem, rewardName);
                    // Reload scene to refresh points
                    stage.setScene(createLoyaltyScene());
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Redemption failed: " + ex.getMessage()).show();
                }
            });

            rewardsGrid.add(rewardCard, i % 2, i / 2);
        }
        rewardsSection.getChildren().addAll(rewardsHeader, rewardsGrid);

        // Activity panel
        VBox activitySection = new VBox(14);
        activitySection.setMinWidth(280); activitySection.setMaxWidth(320);
        Label activityHeader = new Label("Recent Activity");
        activityHeader.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #5D4A3A;");

        VBox activityCard = new VBox(0);
        activityCard.setStyle(cardStyle());

        if (history != null && !history.isEmpty()) {
            for (int i = 0; i < history.size(); i++) {
                LoyaltyActivity la = history.get(i);
                String delta = (la.getPointsChange() >= 0 ? "+" : "") + la.getPointsChange();
                activityCard.getChildren().add(makeActivityRow(
                    la.getDescription(), delta, la.getCreatedAt().toString().substring(0, 10),
                    i < history.size() - 1));
            }
        } else {
            activityCard.getChildren().add(new Label("No recent activity."));
        }

        // Ways to earn card
        VBox earnCard = new VBox(10);
        earnCard.setStyle("-fx-background-color: rgba(212,175,55,0.1); -fx-background-radius: 12px; -fx-padding: 18;");
        Label earnTitle = new Label("Ways to Earn Points");
        earnTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #5D4A3A;");
        earnCard.getChildren().add(earnTitle);
        for (String tip : new String[]{
            "10 points per $1 spent", "50 points for each review",
            "100 points for referrals", "200 bonus on birthdays"}) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            Label dot = new Label("●");
            dot.setStyle("-fx-text-fill: #D4AF37; -fx-font-size: 10px;");
            Label lbl = new Label(tip);
            lbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #5D4A3A;");
            row.getChildren().addAll(dot, lbl);
            earnCard.getChildren().add(row);
        }

        activitySection.getChildren().addAll(activityHeader, activityCard, earnCard);
        bottomRow.getChildren().addAll(rewardsSection, activitySection);

        content.getChildren().addAll(header, kpiRow, tierSection, bottomRow);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #FDF8F0; -fx-background: #FDF8F0;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.getChildren().addAll(nav, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());
        return scene;
    }

    // ─── Tier helpers ───
    private String getTierName(int pts) {
        if (pts >= 4000) {
			return "Platinum";
		}
        if (pts >= 2000) {
			return "Gold";
		}
        if (pts >= 1000) {
			return "Silver";
		}
        return "Bronze";
    }
    private String getNextTierName(int pts) {
        if (pts >= 4000) {
			return "Max";
		}
        if (pts >= 2000) {
			return "Platinum";
		}
        if (pts >= 1000) {
			return "Gold";
		}
        return "Silver";
    }
    private String getTierColor(int pts) {
        if (pts >= 4000) {
			return "#B0C4DE";
		}
        if (pts >= 2000) {
			return "#D4AF37";
		}
        if (pts >= 1000) {
			return "#C0C0C0";
		}
        return "#CD7F32";
    }
    private int getPointsToNextTier(int pts) {
        if (pts >= 4000) {
			return 0;
		}
        if (pts >= 2000) {
			return 4000 - pts;
		}
        if (pts >= 1000) {
			return 2000 - pts;
		}
        return 1000 - pts;
    }
    private double getProgress(int pts) {
        if (pts >= 4000) {
			return 1.0;
		}
        if (pts >= 2000) {
			return (double)(pts - 2000) / 2000;
		}
        if (pts >= 1000) {
			return (double)(pts - 1000) / 1000;
		}
        return (double)pts / 1000;
    }

    // ─── UI helpers ───
    private VBox makeKPICard(String label, String value, String sub, String color) {
        VBox card = new VBox(10);
        card.setStyle(cardStyle());
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #5D4A3A; -fx-opacity: 0.7;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label subLbl = new Label(sub);
        subLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #5D4A3A; -fx-opacity: 0.6;");
        card.getChildren().addAll(lbl, val, subLbl);
        return card;
    }

    private VBox makeTierCard(String tier, String color) {
        VBox card = new VBox(10);
        card.setStyle(cardStyle());
        Label lbl = new Label("Current Tier");
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #5D4A3A; -fx-opacity: 0.7;");
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Label val = new Label(tier);
        val.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label crown = new Label("👑");
        crown.setStyle("-fx-font-size: 22px;");
        row.getChildren().addAll(val, crown);
        Label badge = new Label("Premium Member");
        badge.setStyle("-fx-background-color: rgba(212,175,55,0.15); -fx-text-fill: " + color + ";" +
            "-fx-background-radius: 20px; -fx-padding: 4 12; -fx-font-size: 12px;");
        card.getChildren().addAll(lbl, row, badge);
        return card;
    }

    private VBox makeProgressCard(String nextTier, int pointsToNext, double progress) {
        VBox card = new VBox(10);
        card.setStyle(cardStyle());
        Label lbl = new Label("Progress to " + nextTier);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #5D4A3A; -fx-opacity: 0.7;");
        Label pts = new Label(pointsToNext > 0 ? String.format("%,d points to go", pointsToNext) : "Max tier reached!");
        pts.setStyle("-fx-font-size: 16px; -fx-text-fill: #5D4A3A;");
        // Progress bar
        StackPane bar = new StackPane();
        bar.setMaxHeight(10); bar.setMinHeight(10);
        bar.setStyle("-fx-background-color: #F0F2F5; -fx-background-radius: 5px;");
        HBox fill = new HBox();
        fill.setStyle("-fx-background-color: #D4AF37; -fx-background-radius: 5px;");
        double w = Math.min(progress, 1.0) * 100;
        fill.setPrefWidth(w);
        bar.getChildren().add(fill);
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);
        Label pct = new Label(String.format("%.0f%% complete", progress * 100));
        pct.setStyle("-fx-font-size: 12px; -fx-text-fill: #5D4A3A; -fx-opacity: 0.6;");
        card.getChildren().addAll(lbl, pts, bar, pct);
        return card;
    }

    private HBox makeTierBadge(String name, String color, int minPts, boolean active) {
        VBox card = new VBox(8);
        card.setStyle(cardStyle() + (active ?
            "-fx-border-color: " + color + "; -fx-border-width: 2; -fx-border-radius: 12px;" : ""));
        card.setAlignment(Pos.CENTER_LEFT);
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label dot = new Label("●");
        dot.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 14px;");
        Label nm = new Label(name);
        nm.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #5D4A3A;");
        row.getChildren().addAll(dot, nm);
        Label pts = new Label(String.format("%,d+ points", minPts));
        pts.setStyle("-fx-font-size: 12px; -fx-text-fill: #5D4A3A; -fx-opacity: 0.7;");
        card.getChildren().addAll(row, pts);
        HBox wrapper = new HBox(card);
        HBox.setHgrow(card, Priority.ALWAYS);
        return wrapper;
    }

    private VBox makeRewardCard(String title, String desc, int points, boolean canRedeem, boolean comingSoon) {
        VBox card = new VBox(16);
        card.setStyle(cardStyle() + (comingSoon ? "-fx-opacity: 0.5;" : ""));
        HBox top = new HBox(14);
        top.setAlignment(Pos.TOP_LEFT);
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(46, 46);
        iconBox.setStyle("-fx-background-radius: 10px; -fx-background-color: " +
            (canRedeem ? "#D4AF37" : "#FDF8F0") + ";");
        Label iconLbl = new Label(comingSoon ? "👑" : canRedeem ? "🎁" : "⚡");
        iconLbl.setStyle("-fx-font-size: 20px;");
        iconBox.getChildren().add(iconLbl);
        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label t = new Label(title);
        t.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #5D4A3A;");
        t.setWrapText(true);
        Label d = new Label(desc);
        d.setStyle("-fx-font-size: 12px; -fx-text-fill: #5D4A3A; -fx-opacity: 0.7;");
        Label p = new Label(String.format("%,d points", points));
        p.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #D4AF37;");
        info.getChildren().addAll(t, d, p);
        top.getChildren().addAll(iconBox, info);

        Button btn = new Button(comingSoon ? "Coming Soon" : canRedeem ? "Redeem" : "Not Enough Points");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(38);
        btn.setDisable(!canRedeem);
        btn.setStyle(
            "-fx-background-color: " + (canRedeem ? "#D4AF37" : "#A8978A") + ";" +
            "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;" +
            "-fx-background-radius: 8px; -fx-cursor: " + (canRedeem ? "hand" : "default") + ";" +
            (canRedeem ? "" : "-fx-opacity: 0.55;")
        );
        card.getChildren().addAll(top, btn);
        return card;
    }

    private HBox makeActivityRow(String desc, String points, String date, boolean hasDivider) {
        VBox row = new VBox(4);
        row.setPadding(new Insets(14, 18, 14, 18));
        if (hasDivider) {
            row.setStyle("-fx-border-color: transparent transparent rgba(93,74,58,0.1) transparent; -fx-border-width: 1;");
        }
        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);
        Label descLbl = new Label(desc);
        descLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #5D4A3A;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label ptsLbl = new Label(points);
        ptsLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " +
            (points.startsWith("+") ? "#7A9E6B" : "#B14A3A") + ";");
        top.getChildren().addAll(descLbl, sp, ptsLbl);
        Label dateLbl = new Label(date);
        dateLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #5D4A3A; -fx-opacity: 0.5;");
        row.getChildren().addAll(top, dateLbl);
        return new HBox(row);
    }

    private StackPane makeIconBox(String color, String svgData) {
        StackPane box = new StackPane();
        box.setPrefSize(48, 48);
        box.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10px;");
        SVGPath icon = new SVGPath();
        icon.setContent(svgData);
        icon.setStroke(Color.WHITE);
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        box.getChildren().add(icon);
        return box;
    }

    private String cardStyle() {
        return "-fx-background-color: #FFFFFF; -fx-background-radius: 14px;" +
               "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.07), 16, 0, 0, 4);" +
               "-fx-padding: 22;";
    }
    private String secondaryBtnStyle() {
        return "-fx-background-color: rgba(93,74,58,0.1); -fx-text-fill: #5D4A3A;" +
               "-fx-font-size: 13px; -fx-background-radius: 8px;" +
               "-fx-padding: 8 16; -fx-cursor: hand; -fx-border-style: none;";
    }
}
