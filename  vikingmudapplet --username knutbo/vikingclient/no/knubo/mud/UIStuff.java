package no.knubo.mud;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicIconFactory;

public class UIStuff {

	static class IconProxy implements InvocationHandler {

		private Object obj;

		public static Object newInstance(Object obj) {
			return java.lang.reflect.Proxy.newProxyInstance(obj.getClass()
					.getClassLoader(), obj.getClass().getInterfaces(),
					new IconProxy(obj));
		}

		private IconProxy(Object obj) {
			this.obj = obj;
		}

		public Object invoke(Object proxy, Method m, Object[] args)
				throws Throwable {
			Object result;
			try {
				if (args != null && args.length >= 2
						&& args[1] instanceof Graphics) {
					((Graphics) args[1]).setColor(Color.black);
				}

				result = m.invoke(obj, args);
				if (args != null && args.length >= 2
						&& args[1] instanceof Graphics) {
					((Graphics) args[1]).setColor(Color.white);
				}
			} catch (InvocationTargetException e) {
				throw e.getTargetException();
			} catch (Exception e) {
				throw new RuntimeException("unexpected invocation exception: "
						+ e.getMessage());
			}
			return result;
		}
	}

	public static void setupUI() {
		if (false) {
			String laf = "";
			laf = "javax.swing.plaf.metal.MetalLookAndFeel";
			try {
				UIManager.setLookAndFeel(laf);
			} catch (Exception e2) {
				System.out.println(e2);
			}
		}

		Font menuFont = new Font("Verdana", Font.PLAIN, 12);
		UIManager.put("Menu.font", menuFont);
		UIManager.put("MenuItem.font", menuFont);
		UIManager.put("CheckBoxMenuItem.font", menuFont);

		Color bgPanel = new Color(0xCC, 0x33, 0x00);
		Color bgColor = new Color(0x99, 0x00, 0x00);
		UIManager.put("Button.background", bgColor);
		UIManager.put("Panel.background", bgColor);

		UIManager.put("TextPane.background", bgPanel);
		UIManager.put("TextPane.foreground", new Color(0xFF, 0xFF, 0xCC));

		UIManager.put("Label.foreground", Color.white);

		UIManager.put("Tree.background", bgColor);
		UIManager.put("Tree.textBackground", bgColor);
		UIManager.put("Tree.textForeground", Color.white);

		UIManager.put("Table.background", bgPanel);
		UIManager.put("Table.foreground", Color.white);
		UIManager.put("Viewport.background", bgPanel);

		// UIManager.put("TableHeader.background", bgColor);
		// UIManager.put("TableHeader.foreground", Color.white);

		UIManager.put("MenuBar.background", bgColor);
		UIManager.put("MenuBar.foreground", Color.white);
		UIManager.put("Menu.background", bgColor);
		UIManager.put("Menu.foreground", Color.white);
		UIManager.put("MenuItem.background", bgColor);
		UIManager.put("MenuItem.foreground", Color.white);
		UIManager.put("CheckBoxMenuItem.background", bgColor);
		UIManager.put("CheckBoxMenuItem.foreground", Color.white);

		Icon icon = BasicIconFactory.getCheckBoxMenuItemIcon();
		Object iconProxy = IconProxy.newInstance(icon);
		UIManager.put("CheckBoxMenuItem.checkIcon", iconProxy);

	}

}
