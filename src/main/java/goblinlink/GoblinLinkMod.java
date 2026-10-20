    @Override
    public void loadContent() {
        super.loadContent();

        goblinLinkBlock = new GoblinLinkBlock("goblin-link");

        TechTree.node(Blocks.siliconSmelter, () -> {
            node(goblinLinkBlock, with(
                Items.silicon, 250,
                Items.lead, 200,
                Items.copper, 400
            ), () -> {
                // Здесь можно добавлять дочерние элементы, если они нужны
            });
        });
}
