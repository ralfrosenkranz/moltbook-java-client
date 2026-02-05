package de.ralfrosenkranz.moltbook.demo.swing;

import javax.swing.*;
import java.util.Properties;

final class MoltbookFrame extends JFrame {

    MoltbookFrame(ClientManager cm, Properties props) {
        super("Moltbook Swing Client");

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Home", new HomePanel(cm));
        tabs.addTab("Agents", new AgentsPanel(cm));
        tabs.addTab("Posts", new PostsPanel(cm));
        tabs.addTab("Comments", new CommentsPanel(cm));
        tabs.addTab("Submolts", new SubmoltsPanel(cm));
        tabs.addTab("Following", new FollowingPanel(cm));
        tabs.addTab("Search", new SearchPanel(cm));
        tabs.addTab("Voting", new VotingPanel(cm));
        tabs.addTab("Raw", new RawPanel(cm));
        tabs.addTab("Settings / Register", new SettingsPanel(cm, props));

        setContentPane(tabs);
    }
}
