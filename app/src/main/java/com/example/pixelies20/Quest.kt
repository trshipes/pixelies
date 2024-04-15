package com.example.pixelies20
import android.content.Context
import android.os.Looper
import android.os.Handler
//^only need those if i do the demo version that i needed for original debug, i guess we dont need these anymore but i'll leave for now
import androidx.appcompat.app.AlertDialog
import java.io.Serializable



data class Quest(
    val title: String,
    val description: Map<String, String>,
    val action: Map<String, String>,
    val reward: Map<String, Reward>
) : Serializable


data class Reward(
    val moodBoost: Int,
    val caloriesBurned: Int
) : Serializable


object QuestRepository {
    val quests = listOf(
        Quest(
            title = "The Dragon's Hoard",
            description = mapOf(
                "Easy" to "A dragon's treasure is said to be hidden nearby. Scale the rocky terrain with 30 mountain climbers to uncover the hoard.",
                "Medium" to "A fierce dragon guards its hoard atop a steep peak. Undertake 50 mountain climbers to claim the riches.",
                "Hard" to "Face the dragon's wrath and the perilous climb. Complete 70 mountain climbers to seize the treasure."
            ),
            action = mapOf(
                "Easy" to "30 mountain climbers",
                "Medium" to "50 mountain climbers",
                "Hard" to "70 mountain climbers"
            ),
            reward = mapOf(
                "Easy" to Reward(moodBoost = 1, caloriesBurned = 60),
                "Medium" to Reward(moodBoost = 2, caloriesBurned = 100),
                "Hard" to Reward(moodBoost = 3, caloriesBurned = 140)
            )
        ),
        Quest(
            title = "The Goblin's Challenge",
            description = mapOf(
                "Easy" to "A mischievous goblin challenges you to a race! Sprint for 30 seconds to outrun the creature and claim victory.",
                "Medium" to "A mischievous goblin challenges you to a race! Sprint for 1 minute to outrun the creature and claim victory.",
                "Hard" to "A mischievous goblin challenges you to a race! Sprint for 2 minutes to outrun the creature and claim victory."
            ),
            action = mapOf(
                "Easy" to "Sprint for 30 seconds",
                "Medium" to "Sprint for 1 minute",
                "Hard" to "Sprint for 2 minutes"
            ),
            reward = mapOf(
                "Easy" to Reward(moodBoost = 1, caloriesBurned = 8),
                "Medium" to Reward(moodBoost = 2, caloriesBurned = 15),
                "Hard" to Reward(moodBoost = 3, caloriesBurned = 30)
            )
        ),
        Quest(
            title = "The Dragon's Breath",
            description = mapOf(
                "Easy" to "A dragon threatens the village! Perform 50 jumping jacks to mimic the dragon's breath and scare it away.",
                "Medium" to "A dragon threatens the village! Perform 75 jumping jacks to mimic the dragon's breath and scare it away.",
                "Hard" to "A dragon threatens the village! Perform 100 jumping jacks to mimic the dragon's breath and scare it away."
            ),
            action = mapOf(
                "Easy" to "Perform 50 jumping jacks",
                "Medium" to "Perform 75 jumping jacks",
                "Hard" to "Perform 100 jumping jacks"
            ),
            reward = mapOf(
                "Easy" to Reward(moodBoost = 1, caloriesBurned = 45),
                "Medium" to Reward(moodBoost = 2, caloriesBurned = 60),
                "Hard" to Reward(moodBoost = 3, caloriesBurned = 90)
            )
        ),
        Quest(
            title = "The Forest of Shadows",
            description = mapOf(
                "Easy" to "Navigate through the dark forest by doing 20 lunges. Each lunge lights up a path to guide you out.",
                "Medium" to "Navigate through the dark forest by doing 35 lunges. Each lunge lights up a path to guide you out.",
                "Hard" to "Navigate through the dark forest by doing 50 lunges. Each lunge lights up a path to guide you out."
            ),
            action = mapOf(
                "Easy" to "Perform 20 lunges",
                "Medium" to "Perform 35 lunges",
                "Hard" to "Perform 50 lunges"
            ),
            reward = mapOf(
                "Easy" to Reward(moodBoost = 1, caloriesBurned = 40),
                "Medium" to Reward(moodBoost = 2, caloriesBurned = 55),
                "Hard" to Reward(moodBoost = 3, caloriesBurned = 70)
            )
        ),
        Quest(
            title = "The Mountain Climb",
            description = mapOf(
                "Easy" to "A treasure awaits atop a steep mountain. Climb stairs or a hill for 5 minutes to reach the summit.",
                "Medium" to "A treasure awaits atop a steep mountain. Climb stairs or a hill for 10 minutes to reach the summit.",
                "Hard" to "A treasure awaits atop a steep mountain. Climb stairs or a hill for 15 minutes to reach the summit."
            ),
            action = mapOf(
                "Easy" to "Climb stairs for 5 minutes",
                "Medium" to "Climb stairs for 10 minutes",
                "Hard" to "Climb stairs for 15 minutes"
            ),
            reward = mapOf(
                "Easy" to Reward(moodBoost = 1, caloriesBurned = 50),
                "Medium" to Reward(moodBoost = 2, caloriesBurned = 75),
                "Hard" to Reward(moodBoost = 3, caloriesBurned = 100)
            )
        ),
        Quest(
            title = "The Warrior's Dance",
            description = mapOf(
                "Easy" to "Perform a warrior's dance to prepare for battle. Dance for 5 minutes to sharpen your skills.",
                "Medium" to "Perform a warrior's dance to prepare for battle. Dance for 10 minutes to sharpen your skills.",
                "Hard" to "Perform a warrior's dance to prepare for battle. Dance for 15 minutes to sharpen your skills."
            ),
            action = mapOf(
                "Easy" to "Dance for 5 minutes",
                "Medium" to "Dance for 10 minutes",
                "Hard" to "Dance for 15 minutes"
            ),
            reward = mapOf(
                "Easy" to Reward(moodBoost = 1, caloriesBurned = 50),
                "Medium" to Reward(moodBoost = 2, caloriesBurned = 75),
                "Hard" to Reward(moodBoost = 3, caloriesBurned = 100)
            )
        ),
        Quest(
            title = "The Archer's Aim",
            description = mapOf(
                "Easy" to "Practice your archery skills by doing 20 arm circles each way to strengthen your bow arm.",
                "Medium" to "Practice your archery skills by doing 30 arm circles each way to strengthen your bow arm.",
                "Hard" to "Practice your archery skills by doing 40 arm circles each way to strengthen your bow arm."
            ),
            action = mapOf(
                "Easy" to "Perform 20 arm circles each way",
                "Medium" to "Perform 30 arm circles each way",
                "Hard" to "Perform 40 arm circles each way"
            ),
            reward = mapOf(
                "Easy" to Reward(moodBoost = 1, caloriesBurned = 20),
                "Medium" to Reward(moodBoost = 2, caloriesBurned = 30),
                "Hard" to Reward(moodBoost = 3, caloriesBurned = 40)
            )
        ),
        Quest(
            title = "The Rogue's Agility",
            description = mapOf(
                "Easy" to "Demonstrate your agility by doing 30 seconds of quick feet drills.",
                "Medium" to "Demonstrate your agility by doing 1 minute of quick feet drills.",
                "Hard" to "Demonstrate your agility by doing 1.5 minutes of quick feet drills."
            ),
            action = mapOf(
                "Easy" to "Do 30 seconds of quick feet drills",
                "Medium" to "Do 1 minute of quick feet drills",
                "Hard" to "Do 1.5 minutes of quick feet drills"
            ),
            reward = mapOf(
                "Easy" to Reward(moodBoost = 1, caloriesBurned = 30),
                "Medium" to Reward(moodBoost = 2, caloriesBurned = 45),
                "Hard" to Reward(moodBoost = 3, caloriesBurned = 60)
            )
        ),
        Quest(
            title = "The Mage's Meditation",
            description = mapOf(
                "Easy" to "Meditate for 5 minutes to recharge your magical energy.",
                "Medium" to "Meditate for 10 minutes to recharge your magical energy.",
                "Hard" to "Meditate for 15 minutes to recharge your magical energy."
            ),
            action = mapOf(
                "Easy" to "Meditate for 5 minutes",
                "Medium" to "Meditate for 10 minutes",
                "Hard" to "Meditate for 15 minutes"
            ),
            reward = mapOf(
                "Easy" to Reward(moodBoost = 1, caloriesBurned = 10),
                "Medium" to Reward(moodBoost = 2, caloriesBurned = 20),
                "Hard" to Reward(moodBoost = 3, caloriesBurned = 30)
            )
        ),
        Quest(
            title = "The Paladin's March",
            description = mapOf(
                "Easy" to "March in place with high knees for 5 minutes to prepare for the crusade.",
                "Medium" to "March in place with high knees for 10 minutes to prepare for the crusade.",
                "Hard" to "March in place with high knees for 15 minutes to prepare for the crusade."
            ),
            action = mapOf(
                "Easy" to "March in place with high knees for 5 minutes",
                "Medium" to "March in place with high knees for 10 minutes",
                "Hard" to "March in place with high knees for 15 minutes"
            ),
            reward = mapOf(
                "Easy" to Reward(moodBoost = 1, caloriesBurned = 50),
                "Medium" to Reward(moodBoost = 2, caloriesBurned = 75),
                "Hard" to Reward(moodBoost = 3, caloriesBurned = 100)
            )
        ),
        Quest(
            title = "The Healer's Garden",
            description = mapOf(
                "Easy" to "Tend to your herb garden with 5 minutes of gardening activities.",
                "Medium" to "Tend to your herb garden with 10 minutes of gardening activities.",
                "Hard" to "Tend to your herb garden with 15 minutes of gardening activities."
            ),
            action = mapOf(
                "Easy" to "Garden for 5 minutes",
                "Medium" to "Garden for 10 minutes",
                "Hard" to "Garden for 15 minutes"
            ),
            reward = mapOf(
                "Easy" to Reward(moodBoost = 1, caloriesBurned = 30),
                "Medium" to Reward(moodBoost = 2, caloriesBurned = 45),
                "Hard" to Reward(moodBoost = 3, caloriesBurned = 60)
            )
        ),
        Quest(
            title = "The Enchanted Forest Sprint",
            description = mapOf(
                "Easy" to "A fairy whispers of a hidden glade. Sprint for 2 minutes to find this magical place.",
                "Medium" to "A fairy whispers of a hidden glade. Sprint for 5 minutes to find this magical place.",
                "Hard" to "A fairy whispers of a hidden glade. Sprint for 8 minutes to find this magical place."
            ),
            action = mapOf(
                "Easy" to "Sprint for 2 minutes",
                "Hedium" to "Sprint for 5 minutes",
                "Mard" to "Sprint for 8 minutes"
            ),
            reward = mapOf(
                "Easy" to Reward(moodBoost = 1, caloriesBurned = 40),
                "Medium" to Reward(moodBoost = 2, caloriesBurned = 70),
                "Hard" to Reward(moodBoost = 3, caloriesBurned = 100)
            )
        ),
        Quest(
            title = "The Lich's Lair",
            description = mapOf(
                "Easy" to "A powerful Lich threatens the land. Perform 20 burpees to vanquish this foe.",
                "Medium" to "A powerful Lich threatens the land. Perform 30 burpees to vanquish this foe.",
                "Hard" to "A powerful Lich threatens the land. Perform 40 burpees to vanquish this foe."
            ),
            action = mapOf(
                "Easy" to "Perform 20 burpees",
                "Medium" to "Perform 30 burpees",
                "Hard" to "Perform 40 burpees"
            ),
            reward = mapOf(
                "Easy" to Reward(moodBoost = 1, caloriesBurned = 50),
                "Medium" to Reward(moodBoost = 2, caloriesBurned = 75),
                "Hard" to Reward(moodBoost = 3, caloriesBurned = 100)
            )
        ),
        Quest(
            title = "The Mermaid's Swim",
            description = mapOf(
                "Easy" to "A mermaid needs your help to retrieve her lost pearl. Swim for 5 minutes to find it.",
                "Medium" to "A mermaid needs your help to retrieve her lost pearl. Swim for 10 minutes to find it.",
                "Hard" to "A mermaid needs your help to retrieve her lost pearl. Swim for 15 minutes to find it."
            ),
            action = mapOf(
                "Easy" to "Swim for 5 minutes",
                "Medium" to "Swim for 10 minutes",
                "Hard" to "Swim for 15 minutes"
            ),
            reward = mapOf(
                "Easy" to Reward(moodBoost = 1, caloriesBurned = 60),
                "Medium" to Reward(moodBoost = 2, caloriesBurned = 90),
                "Hard" to Reward(moodBoost = 3, caloriesBurned = 120)
            )
        ),
        Quest(
            title = "The Griffin's Flight",
            description = mapOf(
                "Easy" to "A griffin offers you a ride. Do 20 arm raises to keep balance as it soars through the sky.",
                "Medium" to "A griffin offers you a ride. Do 30 arm raises to keep balance as it soars through the sky.",
                "Hard" to "A griffin offers you a ride. Do 40 arm raises to keep balance as it soars through the sky."
            ),
            action = mapOf(
                "Easy" to "Do 20 arm raises",
                "Medium" to "Do 30 arm raises",
                "Hard" to "Do 40 arm raises"
            ),
            reward = mapOf(
                "Easy" to Reward(moodBoost = 1, caloriesBurned = 20),
                "Medium" to Reward(moodBoost = 2, caloriesBurned = 30),
                "Hard" to Reward(moodBoost = 3, caloriesBurned = 40)
            )
        ),
        Quest(
            title = "The Sorcerer's Spell",
            description = mapOf(
                "Easy" to "A sorcerer needs your help to complete a powerful spell. Perform 20 squats to gather the arcane energy required.",
                "Medium" to "A sorcerer needs your help to complete a powerful spell. Perform 30 squats to gather the arcane energy required.",
                "Hard" to "A sorcerer needs your help to complete a powerful spell. Perform 40 squats to gather the arcane energy required."
            ),
            action = mapOf(
                "Easy" to "Perform 20 squats",
                "Medium" to "Perform 30 squats",
                "Hard" to "Perform 40 squats"
            ),
            reward = mapOf(
                "Easy" to Reward(moodBoost = 1, caloriesBurned = 20),
                "Medium" to Reward(moodBoost = 2, caloriesBurned = 30),
                "Hard" to Reward(moodBoost = 3, caloriesBurned = 40)
            )
        ),
        Quest(
            title = "The Phoenix's Rebirth",
            description = mapOf(
                "Easy" to "Witness the rebirth of a phoenix. Perform a 5-minute yoga session to emulate the grace and serenity of this immortal bird.",
                "Medium" to "Witness the rebirth of a phoenix. Perform a 10-minute yoga session to emulate the grace and serenity of this immortal bird.",
                "Hard" to "Witness the rebirth of a phoenix. Perform a 15-minute yoga session to emulate the grace and serenity of this immortal bird."
            ),
            action = mapOf(
                "Easy" to "Perform a 5-minute yoga session",
                "Medium" to "Perform a 10-minute yoga session",
                "Hard" to "Perform a 15-minute yoga session"
            ),
            reward = mapOf(
                "Easy" to Reward(moodBoost = 1, caloriesBurned = 15),
                "Medium" to Reward(moodBoost = 2, caloriesBurned = 30),
                "Hard" to Reward(moodBoost = 3, caloriesBurned = 45)
            )
        ),
        Quest(
            title = "The Vampire's Hunt",
            description = mapOf(
                "Easy" to "A vampire is on the prowl. Do 30 seconds of shadow boxing to fend off this nocturnal predator and protect the village.",
                "Medium" to "A vampire is on the prowl. Do 1 minute of shadow boxing to fend off this nocturnal predator and protect the village.",
                "Hard" to "A vampire is on the prowl. Do 2 minutes of shadow boxing to fend off this nocturnal predator and protect the village."
            ),
            action = mapOf(
                "Easy" to "Do 30 seconds of shadow boxing",
                "Medium" to "Do 1 minute of shadow boxing",
                "Hard" to "Do 2 minutes of shadow boxing"
            ),
            reward = mapOf(
                "Easy" to Reward(moodBoost = 1, caloriesBurned = 15),
                "Medium" to Reward(moodBoost = 2, caloriesBurned = 30),
                "Hard" to Reward(moodBoost = 3, caloriesBurned = 45)
            )
        ),
        Quest(
            title = "The Elf's Arrow",
            description = mapOf(
                "Easy" to "An elf needs your assistance to craft the perfect arrow. Do 10 plank shoulder taps to steady your hands and aid in this delicate task.",
                "Medium" to "An elf needs your assistance to craft the perfect arrow. Do 20 plank shoulder taps to steady your hands and aid in this delicate task.",
                "Hard" to "An elf needs your assistance to craft the perfect arrow. Do 30 plank shoulder taps to steady your hands and aid in this delicate task."
            ),
            action = mapOf(
                "Easy" to "Do 10 plank shoulder taps",
                "Medium" to "Do 20 plank shoulder taps",
                "Hard" to "Do 30 plank shoulder taps"
            ),
            reward = mapOf(
                "Easy" to Reward(moodBoost = 1, caloriesBurned = 10),
                "Medium" to Reward(moodBoost = 2, caloriesBurned = 20),
                "Hard" to Reward(moodBoost = 3, caloriesBurned = 30)
            )
        ),
        Quest(
            title = "The Golem's Heart",
            description = mapOf(
                "Easy" to "A golem has lost its magical heart. Perform 10 lunges to navigate the rocky terrain and retrieve it.",
                "Medium" to "A golem has lost its magical heart. Perform 20 lunges to navigate the rocky terrain and retrieve it.",
                "Hard" to "A golem has lost its magical heart. Perform 30 lunges to navigate the rocky terrain and retrieve it."
            ),
            action = mapOf(
                "Easy" to "Perform 10 lunges",
                "Medium" to "Perform 20 lunges",
                "Hard" to "Perform 30 lunges"
            ),
            reward = mapOf(
                "Easy" to Reward(moodBoost = 1, caloriesBurned = 20),
                "Medium" to Reward(moodBoost = 2, caloriesBurned = 40),
                "Hard" to Reward(moodBoost = 3, caloriesBurned = 60)
            )
        ),
        Quest(
            title = "The Siren's Song",
            description = mapOf(
                "Easy" to "A siren's song lures sailors to their doom. Do 30 seconds of high knees to resist the enchanting melody and navigate safely.",
                "Medium" to "A siren's song lures sailors to their doom. Do 45 seconds of high knees to resist the enchanting melody and navigate safely.",
                "Hard" to "A siren's song lures sailors to their doom. Do 60 seconds of high knees to resist the enchanting melody and navigate safely."
            ),
            action = mapOf(
                "Easy" to "Perform 30 seconds of high knees",
                "Medium" to "Perform 45 seconds of high knees",
                "Hard" to "Perform 60 seconds of high knees"
            ),
            reward = mapOf(
                "Easy" to Reward(moodBoost = 1, caloriesBurned = 20),
                "Medium" to Reward(moodBoost = 2, caloriesBurned = 30),
                "Hard" to Reward(moodBoost = 3, caloriesBurned = 45)
            )
        ),
        Quest(
            title = "The Witch's Brew",
            description = mapOf(
                "Easy" to "A witch requires rare ingredients for her potion. Perform a 3-minute dance to gather the mystical herbs without being noticed.",
                "Medium" to "A witch requires rare ingredients for her potion. Perform a 5-minute dance to gather the mystical herbs without being noticed.",
                "Hard" to "A witch requires rare ingredients for her potion. Perform a 7-minute dance to gather the mystical herbs without being noticed."
            ),
            action = mapOf(
                "Easy" to "Dance for 3 minutes",
                "Medium" to "Dance for 5 minutes",
                "Hard" to "Dance for 7 minutes"
            ),
            reward = mapOf(
                "Easy" to Reward(moodBoost = 1, caloriesBurned = 25),
                "Medium" to Reward(moodBoost = 2, caloriesBurned = 40),
                "Hard" to Reward(moodBoost = 3, caloriesBurned = 55)
            )
        ),
        Quest(
            title = "The Fairy Ring",
            description = mapOf(
                "Easy" to "A circle of fairies invites you to join their dance. Do 20 high knees to match their swift, airy movements and gain their favor.",
                "Medium" to "A circle of fairies invites you to join their dance. Do 30 high knees to match their swift, airy movements and gain their favor.",
                "Hard" to "A circle of fairies invites you to join their dance. Do 40 high knees to match their swift, airy movements and gain their favor."
            ),
            action = mapOf(
                "Easy" to "Perform 20 high knees",
                "Medium" to "Perform 30 high knees",
                "Hard" to "Perform 40 high knees"
            ),
            reward = mapOf(
                "Easy" to Reward(moodBoost = 1, caloriesBurned = 15),
                "Medium" to Reward(moodBoost = 2, caloriesBurned = 22),
                "Hard" to Reward(moodBoost = 3, caloriesBurned = 30)
            )
        ),
        Quest(
            title = "The Crystal Cavern",
            description = mapOf(
                "Easy" to "A cavern filled with glowing crystals beckons you. Do a 2-minute yoga session to attune yourself to their energy and unlock their secrets.",
                "Medium" to "A cavern filled with glowing crystals beckons you. Do a 4-minute yoga session to attune yourself to their energy and unlock their secrets.",
                "Hard" to "A cavern filled with glowing crystals beckons you. Do a 6-minute yoga session to attune yourself to their energy and unlock their secrets."
            ),
            action = mapOf(
                "Easy" to "Yoga for 2 minutes",
                "Medium" to "Yoga for 4 minutes",
                "Hard" to "Yoga for 6 minutes"
            ),
            reward = mapOf(
                "Easy" to Reward(moodBoost = 1, caloriesBurned = 15),
                "Medium" to Reward(moodBoost = 2, caloriesBurned = 25),
                "Hard" to Reward(moodBoost = 3, caloriesBurned = 35)
            )
        ),
            // keep adding more quest (have hundreds more in a word doc- find them when/if there is time)

    )

    fun getRandomQuest(): Quest = quests.random()
}
interface QuestManagerListener {
    fun onQuestStarted(quest: Quest)
    fun onQuestCompleted(quest: Quest, reward: Reward)
    fun onQuestFailed(quest: Quest, message: String)
}
class QuestManager(private val context: Context, private val quests: List<Quest>) {

    // var onQuestCompleted: ((Quest, String) -> Unit)? = null
    var currentQuest: Quest? = null
    private var questStartTime: Long = 0
    // var onQuestFailed: ((Quest, String) -> Unit)? = null

    fun startQuest(quest: Quest, difficulty: String) {
        currentQuest = quest
        questStartTime = System.currentTimeMillis()
        (context as QuestManagerListener).onQuestStarted(quest)
        println("Quest started: ${quest.title} with difficulty $difficulty")
    }

    fun completeQuest(quest: Quest, difficulty: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - questStartTime < 20000) {  // Rushed quest 20 secs bc they are a big fat liar
            AlertDialog.Builder(context)
                .setTitle("Rogue Adventurer...")
                .setMessage("You've faked your quest and your pet is unhappy about the dishonest attempt! Their mood decreased!")
                .setPositiveButton("Continue Quest") { dialog, which ->
                    // If they continue, don't apply any reward yet; just let them continue the quest
                    (context as QuestManagerListener).onQuestFailed(quest, "lie")
                    dialog.dismiss()
                    //would be nice to make the above show the mood decrease before they click continue
                }
                .setNegativeButton("Quit Quest") { dialog, which ->
                    quitQuest(quest, difficulty)
                    dialog.dismiss()
                }
                .show()
        } else {
            // Legit quest completion
            val reward = quest.reward[difficulty] ?: Reward(0, 0)
            (context as QuestManagerListener).onQuestCompleted(quest, reward)
            println("Quest completed: ${quest.title} with difficulty $difficulty")
        }
    }

    fun quitQuest(quest: Quest, difficulty: String) {
        // (context as QuestManagerListener).onQuestFailed(quest, "Your pet feels guilty for not completing the quest. It's a sad day...")
        AlertDialog.Builder(context)
            .setTitle("Quest Quit")
            .setMessage("Your pet feels guilty for not completing the quest. It's a sad day...")
            .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
            .show()
        currentQuest = null
        (context as QuestManagerListener).onQuestFailed(quest, "quit")


    }
}

//nvm dont do this here bc too complicated, but dont delete for now
 /*   private fun updateDailyCaloriesAndMood(caloriesBurned: Int, moodBoost: Int) {
        (context as? PlayRoomActivity)?.let {
            it.caloriesBurnedToday += caloriesBurned
            it.moodLevel =
                Math.min(it.moodLevel + moodBoost, 5)
            it.updateStatsTextViews()
            it.updateHealthBars()
        }
    }
   // private fun showRpgStyleMessage(message: String) {
     //   AlertDialog.Builder(context)
       //     .setTitle("Quest Update")
         //   .setMessage(message)
           // .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            //.show()


}*/
