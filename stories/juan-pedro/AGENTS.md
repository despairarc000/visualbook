# Juan at Pedro — Story continuity instructions

Applicable to the \`stories/juan-pedro/\` subtree. These are rules for this original series only, not for historical folk narratives.

1. Read [SERIES_BIBLE.md](SERIES_BIBLE.md) and [FOLKLORE_RESEARCH.md](FOLKLORE_RESEARCH.md) before writing any episode or changing a character. Folklore is fragmented; do **not** present the original water-rights mystery or cast as universally canonical.
2. Every episode belongs to the same shared **Maligaya** chronology. Follow up the prior ending; keep speaker/voice IDs, map geography, clues, contracts, calendar, relationships and outcomes consistent. Establish new conflicts through the existing world.
3. Respect the motivations and character arc: Juan's laziness should sometimes be an obstacle and sometimes curiosity; Pedro is practical and capable, not a fool; Maria investigates independently. The supporting cast has agency.
4. A \`.story.md\` is the machine-playable text-first script and should use only currently supported directives. Keep advanced staging and unimplemented image/FX control in a separate \`PRODUCTION_NOTES.md\`, never as silently spoken unknown directives.
5. For v0.6 prototype: story source <=100000 chars, <=500 Java BreakIterator utterances. Run \`JuanPedroSeriesTest\` and record measured narrator/character word counts; strive for ~24-minute runtime subject to voice timing and gaps.
6. Update the season bible's episode arc, continuity artifacts and unrevealed facts when major story decisions change. Do not insert arbitrary permanent character deaths, time travel or large genre shifts without a recorded creative rationale.
7. Preserve the ability to adapt historical **motifs** without copying substantial wording from collected editions or distinctive characters/plots created in contemporary stage, television or film adaptations.
8. Maintain two experiences: reader story without directives and VisualBook script with reproducible voice markers. Readers should never need the developer's repository to understand the plot.
