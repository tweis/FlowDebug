<?php

namespace Tweis\TestingPackage\Service;

use Neos\Flow\Annotations as Flow;

class FooService
{
    /**
     * @Flow\Inject
     * @var BarService
     */
    protected $barService;

    public function getFooBar(): string
    {
        return sprintf('Foo%s', $this->barService->getBar());
    }
}
