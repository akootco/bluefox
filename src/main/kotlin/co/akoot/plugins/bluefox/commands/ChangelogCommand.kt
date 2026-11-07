package co.akoot.plugins.bluefox.commands

import co.akoot.plugins.bluefox.BlueFox
import co.akoot.plugins.bluefox.api.CatCommand
import co.akoot.plugins.bluefox.api.changelog.Changelog
import co.akoot.plugins.bluefox.api.changelog.ChangelogManager
import net.kyori.adventure.audience.Audience

object ChangelogCommand : CatCommand(BlueFox.instance, "changelog", "Display the latest changelog!", onCommand = {

    fun sendChangelog(audience: Audience) {
        ChangelogManager.apply {
            clear()

            val bf = Changelog(
                "BlueFox",
                "BlueFox v1.2.1",
                color = "#3bb1ed",
                icon = "https://d1wqzb5bdbcre6.cloudfront.net/a79bbd4fc5b22f16013c9ddaefb2a23c24c9af6fecd0379144fb6c18e86b924b/68747470733a2f2f66696c65732e7374726970652e636f6d2f6c696e6b732f4d44423859574e6a6446387854465a6d52305247656c5178516e4a565932747266475a7358327870646d566655476c4b554552345a32457a59576c6d64564e786133704655315235655535533030785a354e6855796b/6d65726368616e745f69643d616363745f314c56664744467a5431427255636b6b26636c69656e743d50524f445543545f415049"
            )
            val ac = Changelog(
                "AlleyCat",
                "AlleyCat v2.4.3",
                color = "#ed6d3b",
                icon = "https://stripe-camo.global.ssl.fastly.net/ec6d5ead15f68f428b40db340e5aa2e62ac7d668e411bdfab2f68071771b13b6/68747470733a2f2f66696c65732e7374726970652e636f6d2f6c696e6b732f4d44423859574e6a6446387854465a6d52305247656c5178516e4a565932747266475a7358327870646d566652574935535568794e4856306148706f5a324a486455315256564a6e4d32524430307248395465437748/6d65726368616e745f69643d616363745f314c56664744467a5431427255636b6b26636c69656e743d50524f445543545f415049"
            )
            val ch = Changelog(
                "Choco",
                "Choco v1.2.3",
                color = "#9b6a4c",
                image = "https://art.ngfiles.com/images/194000/194297_elemayostudios_ghoastie.png?f1329352313",
                icon = "https://stripe-camo.global.ssl.fastly.net/ca374fda8c07052b524ac4c1c923c43d6c06fb2f3b03be78b8c18cd9e9d85125/68747470733a2f2f66696c65732e7374726970652e636f6d2f6c696e6b732f4d44423859574e6a6446387854465a6d52305247656c5178516e4a565932747266475a7358327870646d56664f44563364316442516e7056566d746e556d645854466c7a54326b786233673030304730307445553569/6d65726368616e745f69643d616363745f314c56664744467a5431427255636b6b26636c69656e743d50524f445543545f415049"
            )

            register(bf)
            register(ac)
            register(ch)

            logChange("BlueFox", "changed the thing")
            logAddition("BlueFox", "added the (secret) thing", true)
            logDeletion("BlueFox", "removed the thing")
            logFix("BlueFox", "fixed the thing")

            logAddition("AlleyCat", "added the thing")
            logDeletion("AlleyCat", "removed the thing")

            logChange("Choco", "changed the thing")
            logChange("Choco", "changed the thing")
        }
        ChangelogManager.send(audience)
        BlueFox.channels["test-minecraft"]?.let { channel -> ChangelogManager.send(channel) }
    }
    noargs {
        //sendChangelog(it.sender)
        true
    }
})