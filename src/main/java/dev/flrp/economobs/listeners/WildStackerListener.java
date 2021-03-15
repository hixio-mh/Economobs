package dev.flrp.economobs.listeners;

import com.bgsoftware.wildstacker.api.events.EntityUnstackEvent;
import com.earth2me.essentials.User;
import dev.flrp.economobs.Economobs;
import dev.flrp.economobs.api.events.EcoGiveEvent;
import dev.flrp.economobs.configuration.StackerType;
import net.ess3.api.MaxMoneyException;
import net.ess3.api.events.UserBalanceUpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.UUID;

public class WildStackerListener implements Listener {

    private final Economobs plugin;

    public WildStackerListener(Economobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void mobStackerEntityDeath(EntityUnstackEvent event) {
        if(plugin.getStackerType() != StackerType.WILDSTACKER) return;
        if(event.getUnstackSource() == null) return;

        UUID id = event.getUnstackSource().getUniqueId();
        Entity entity = plugin.getServer().getEntity(id);
        if(entity.getType() != EntityType.PLAYER) return;

        Player player = (Player) plugin.getServer().getEntity(id);
        User user = plugin.getEssentials().getUser(player);

        HashMap<EntityType, Double> amounts = plugin.getMobDataHandler().getAmounts();
        try {
            double x = applyMultipliers(amounts.get(entity.getType()), entity.getWorld(), itemInHand(player).getType()) * event.getAmount();
            EcoGiveEvent ecoGiveEvent = new EcoGiveEvent(x, (LivingEntity) entity);
            Bukkit.getPluginManager().callEvent(ecoGiveEvent);
            if(!ecoGiveEvent.isCancelled()) {
                giveMoney(user, BigDecimal.valueOf(x).setScale(2, RoundingMode.DOWN), UserBalanceUpdateEvent.Cause.API);
            }
        } catch(MaxMoneyException e) {
            user.sendMessage(plugin.getLocale().parse(plugin.getLanguage().getConfiguration().getString("prefix") + plugin.getLanguage().getConfiguration().getString("economy-max")));
        }
    }

    // Modified essentials method.
    // https://github.com/EssentialsX/Essentials/blob/141512f2f7f8dbe7e844b05244eecdc60018f7f7/Essentials/src/main/java/com/earth2me/essentials/User.java
    public void giveMoney(final User user, final BigDecimal value, final UserBalanceUpdateEvent.Cause cause) throws MaxMoneyException {
        if (value.signum() == 0) {
            return;
        }
        user.setMoney(user.getMoney().add(value), cause);
        user.sendMessage(plugin.getLocale().parse(plugin.getLanguage().getConfiguration().getString("prefix") + plugin.getLanguage().getConfiguration().getString("economy-given"), value.toString()));
    }

    public double applyMultipliers(double value, World world, Material item) {
        if(plugin.getWorldMultiplierList().containsKey(world)) value = value * plugin.getWorldMultiplierList().get(world);
        if(plugin.getWeaponMultiplierList().containsKey(item)) value = value * plugin.getWeaponMultiplierList().get(item);
        return value;
    }

    public ItemStack itemInHand(Player player) {
        if(plugin.getServer().getVersion().contains("1.8")) {
            return player.getItemInHand();
        } else {
            return player.getInventory().getItemInMainHand();
        }
    }

}