package openmods.network.rpc;

import openmods.datastore.IDataVisitor;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class TargetWrapperRegistry implements IDataVisitor<String, Integer> {

	private BiMap<Class<? extends ITargetWrapper>, Integer> wrapperCls = HashBiMap.create();

	@Override
	public void begin(int size) {
		wrapperCls.clear();
	}

	@Override
	public void entry(String clsName, Integer clsId) {
		Class<?> cls;
		try {
			cls = Class.forName(clsName);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(String.format("Failed to load class %s", clsName), e);
		}

		Preconditions.checkArgument(ITargetWrapper.class.isAssignableFrom(cls), "Class %s is not ITargetWrapper", cls);

		try {
			cls.getConstructor();
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(String.format("Class %s has no parameterless constructor", clsName), e);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}

		@SuppressWarnings("unchecked")
		final Class<? extends ITargetWrapper> wrapperCls = (Class<? extends ITargetWrapper>)cls;
		this.wrapperCls.put(wrapperCls, clsId);
	}

	@Override
	public void end() {}

	public int getWrapperId(Class<? extends ITargetWrapper> cls) {
		Integer id = wrapperCls.get(cls);
		Preconditions.checkNotNull(id, "Wrapper class %s is not registered", cls);
		return id;
	}

	public ITargetWrapper createWrapperFromId(int id) {
		Class<? extends ITargetWrapper> cls = wrapperCls.inverse().get(id);
		Preconditions.checkNotNull(cls, "Can't find class for id %s", id);

		try {
			return cls.newInstance();
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}
}
