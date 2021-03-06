package model.member.state.implementations.manipulating;

import javafx.beans.property.ReadOnlyStringProperty;
import manager.LanguageUtility;
import model.member.BattleMember;
import model.member.state.MemberStateIcon;
import model.member.state.RandomPowerMemberState;
import model.member.state.interfaces.IManipulatingMemberState;
import model.member.state.interfaces.IRandomPowerMemberState;

public class ManaRegenerationMemberState extends RandomPowerMemberState implements IRandomPowerMemberState, IManipulatingMemberState {

    public ManaRegenerationMemberState(String name, int duration, boolean activeRounder, BattleMember source, float maxPower, boolean random) {
        super(name, MemberStateIcon.MANA_REGENERATION, duration, activeRounder, source, maxPower, random);
    }

    @Override
    public void apply(BattleMember member) {
        member.setMana(member.getMana() + Math.round(getEffectPower()));
    }

    @Override
    public ReadOnlyStringProperty toStringProperty() {
        return LanguageUtility.getMessageProperty("state.effect.manaRegeneration");
    }
}
