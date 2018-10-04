package com.matt.forgehax.util.command.v2.serializers;

import com.matt.forgehax.util.command.v2.ICmd;
import com.matt.forgehax.util.serialization.ISerializableMutable;

public interface ICmdSerializer<E extends ICmd> extends ISerializableMutable<E> {}
