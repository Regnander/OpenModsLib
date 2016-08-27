package openmods.calc;

import com.google.common.base.Optional;

public interface ICallable<E> {
	void call(Frame<E> frame, Optional<Integer> argumentsCount, Optional<Integer> returnsCount);
}