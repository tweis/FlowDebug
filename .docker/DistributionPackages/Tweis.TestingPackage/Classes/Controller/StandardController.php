<?php

namespace Tweis\TestingPackage\Controller;

use Neos\Flow\Annotations as Flow;
use Neos\Flow\Mvc\Controller\ActionController;
use Tweis\TestingPackage\Service\FooService;

class StandardController extends ActionController
{
    public function indexAction(): void
    {
        $fooService = new FooService();

        $this->view->assign('foos', array(
            'bar', 'baz', $fooService->getFooBar()
        ));
    }
}
