package co.akoot.plugins.bluefox.extensions

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.world.WorldMock
import kotlin.random.Random

class PersistentDataHolderExtensionsTest {

    lateinit var server: ServerMock
    lateinit var plugin: MockBukkit
    lateinit var world: WorldMock
    lateinit var endWorld: WorldMock

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        world = server.addSimpleWorld("world")
        endWorld = server.addSimpleWorld("world_the_end")
    }

    @AfterEach
    fun tearDown() {
        MockBukkit.unmock()
    }


    @Test
    fun `test serialization of Location with random values`() {
        val original = Location(
            world,
            Random.nextDouble(),
            Random.nextDouble(),
            Random.nextDouble(),
            Random.nextFloat(),
            Random.nextFloat()
        )

        val bytes = original.getBytes()
        val restored = getLocation(bytes)

        assertLocationEquals(original, restored)
    }

    @Test
    fun `test serialization of Location with max and min values`() {
        val original =
            Location(endWorld, Double.MAX_VALUE, Double.MIN_VALUE, -Double.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE)

        val bytes = original.getBytes()
        val restored = getLocation(bytes)

        assertLocationEquals(original, restored)
    }

    private fun assertLocationEquals(original: Location, restored: Location) {
        assertEquals(original.world.name, restored.world.name, "Deserialized object's World should match the original")
        assertEquals(original.x, restored.x, "Deserialized object's X should match the original")
        assertEquals(original.y, restored.y, "Deserialized object's Y should match the original")
        assertEquals(original.z, restored.z, "Deserialized object's Z should match the original")
        assertEquals(original.yaw, restored.yaw, "Deserialized object's Yaw should match the original")
        assertEquals(original.pitch, restored.pitch, "Deserialized object's Pitch should match the original")
    }
}