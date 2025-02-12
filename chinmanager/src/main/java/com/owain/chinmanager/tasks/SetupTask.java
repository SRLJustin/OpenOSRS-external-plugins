package com.owain.chinmanager.tasks;

import com.owain.chinmanager.ChinManager;
import com.owain.chinmanager.ChinManagerPlugin;
import com.owain.chinmanager.ChinManagerStates;
import com.owain.chinmanager.magicnumbers.MagicNumberScripts;
import com.owain.chinmanager.utils.IntRandomNumberGenerator;
import com.owain.chintasks.Task;
import io.reactivex.rxjava3.core.ObservableEmitter;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.KEY_TYPED;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.VarClientInt;
import net.runelite.client.callback.ClientThread;

@Slf4j
public class SetupTask implements Task<Void>
{
	private final ChinManager chinManager;
	private final ChinManagerPlugin chinManagerPlugin;
	private final Client client;
	private final ClientThread clientThread;

	@Inject
	SetupTask(ChinManager chinManager, ChinManagerPlugin chinManagerPlugin)
	{
		this.chinManager = chinManager;
		this.chinManagerPlugin = chinManagerPlugin;
		this.client = chinManagerPlugin.getClient();
		this.clientThread = chinManagerPlugin.getClientThread();
	}

	@Override
	public void routine(ObservableEmitter<Void> emitter)
	{
		if (chinManagerPlugin.getExecutorService() == null || chinManagerPlugin.getExecutorService().isShutdown() || chinManagerPlugin.getExecutorService().isTerminated())
		{
			chinManagerPlugin.setExecutorService(Executors.newSingleThreadExecutor());
		}

		chinManagerPlugin.getExecutorService().submit(() ->
		{
			ChinManagerPlugin.shouldSetup = false;

			try
			{
				keyEvent(KeyEvent.KEY_PRESSED, KeyEvent.VK_UP);

				int j = 0;

				do
				{
					Thread.sleep(new IntRandomNumberGenerator(120, 240).nextInt());
					j++;
				}
				while (j < 10);

				keyEvent(KeyEvent.KEY_RELEASED, KeyEvent.VK_UP);

				clientThread.invoke(() -> {
					chinManagerPlugin.transition(ChinManagerStates.IDLE);

					if (client.getVar(VarClientInt.INVENTORY_TAB) != 3)
					{
						client.runScript(MagicNumberScripts.ACTIVE_TAB.getId(), 3);
					}
					chinManager.setCurrentlyActive(chinManager.getNextActive());
				});
			}
			catch (InterruptedException e)
			{
				log.error("Oops!", e);
			}
		});
	}

	public void unsubscribe()
	{
		chinManagerPlugin.getExecutorService().shutdownNow();
	}

	public void keyEvent(int id, int key)
	{
		KeyEvent e = new KeyEvent(
			client.getCanvas(),
			id,
			System.currentTimeMillis(),
			0,
			id == KEY_TYPED ? KeyEvent.VK_UNDEFINED : key,
			id == KEY_TYPED ? (char) key : KeyEvent.CHAR_UNDEFINED
		);

		client.getCanvas().dispatchEvent(e);
	}
}
